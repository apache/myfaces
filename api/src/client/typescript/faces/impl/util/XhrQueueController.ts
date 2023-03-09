import {IAsyncRunnable} from "./AsyncRunnable";
import {ExtLang} from "./Lang";
import debounce = ExtLang.debounce;

/**
 * A simple XHR queue controller
 * following the async op -> next pattern
 * Faces enforces for the XHR handling
 */
export class XhrQueueController<T extends IAsyncRunnable<any>> {
    queue = [];
    taskRunning = false;

    constructor() {
    }

    /**
     * executes or enqueues an element
     * @param runnable the runnable (request) to be enqueued
     * @param timeOut timeout if > 0 which defers the execution
     * until the debounce window for the timeout is closed.
     */
    enqueue(runnable: T, timeOut: number = 0) {
        debounce("xhrQueue", () => {
            const requestHandler = this.enrichRunnable(runnable);
            if (!this.taskRunning) {
                this.signalTaskRunning();
                requestHandler.start();
            } else {
                this.queue.push(requestHandler);
            }
        }, timeOut);
    }

    /**
     * trigger the next element in the queue
     * to be started!
     */
    next() {
        this.updateTaskRunning();
        const next = this.queue.shift();
        next?.start();
    }

    /**
     * clears and resets the queue
     */
    clear() {
        this.queue.length = 0;
        this.updateTaskRunning();
    }

    /**
     * true if queue is empty
     */
    get isEmpty(): boolean {
        return !this.queue.length;
    }

    /**
     * Enriches the incoming async asyncRunnable
     * with the error and next handling
     * (aka: asyncRunnable is done -> next
     *                   error -> clear queue
     * @param asyncRunnable the async runnable which needs enrichment
     * @private
     */
    private enrichRunnable(asyncRunnable: T) {
        /**
         * we can use the Promise pattern asyncrunnable uses
         * to trigger queue control callbacks of next element
         * and clear the queue (theoretically this
         * would work with any promise)
         */
        try {
            return asyncRunnable
                .then(() => this.next())
                .catch((e) => this.handleError(e));
        } catch (e) {
            this.handleError(e);
        }
    }


    /**
     * alerts the queue that a task is running
     *
     * @private
     */
    private signalTaskRunning() {
        this.taskRunning = true;
    }

    /**
     * updates the task running status according to the current queue
     * @private
     */
    private updateTaskRunning() {
        this.taskRunning = !this.isEmpty;
    }

    /**
     * standard error handling
     * we clear the queue and then bomb out
     * @param e
     * @private
     */
    private handleError(e) {
        this.clear();
        throw e;
    }
}