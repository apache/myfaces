export interface IListener<T> {
    (data: T): void;
}
