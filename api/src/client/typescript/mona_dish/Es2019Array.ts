/**
 * Extended array
 */

/**
 * Extended array which adds various es 2019 shim functions to the normal array
 * We must remap all array producing functions in order to keep
 * the delegation active, once we are in!
 */
class Es2019Array_<T>  extends Array<T>{

    _another: T[];

    constructor(...another: T[]) {
        super(...another);
        if((another as any)._another)  {
            this._another = (another as any)._another;
        } else {
            this._another = another;
        }

        //for testing it definitely runs into this branch because we are on es5 level
        //if (!(<any>Array.prototype).flatMap) {
            this.flatMap = (flatMapFun) => this._flatMap(flatMapFun) as any;
        //}
        //if (!(<any>Array.prototype).flat) {
            this.flat = (flatLevel: number = 1) => this._flat(flatLevel);
        //}
    }

    map<U>(callbackfn: (value: T, index: number, array: T[]) => U, thisArg?: any): U[] {
        const ret = Array.prototype.map.call(this._another, callbackfn, thisArg);
        return new (_Es2019Array as any) (... ret);
    }

    concat(...items): T[] {
        const ret = Array.prototype.concat.call(this._another, ...items);
        return new (_Es2019Array as any)(... ret);
    }

    reverse(): T[] {
        const ret = Array.prototype.reverse.call(this._another);
        return new (_Es2019Array as any)(... ret);
    }

    slice(start?: number, end?: number): T[] {
        const ret = Array.prototype.slice.call(this._another, start, end);
        return new (_Es2019Array as any)(...ret);
    }

    splice(start: number, deleteCount?: number): T[] {
        const ret = Array.prototype.splice.call(this._another, start, deleteCount);
        return new (_Es2019Array as any)(...ret);
    }

    filter<S extends T>(predicate: (value: T, index: number, array: T[]) => any, thisArg?: any): S[] {
        const ret = Array.prototype.filter.call(this._another, predicate, thisArg);
        return new (_Es2019Array as any)(...ret);
    }


    reduce(callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) => T, initialValue?: T): T {
        const ret = Array.prototype.reduce.call(this._another, callbackfn, initialValue);
        return ret;
    }

    /*reduceRight(callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) => T, initialValue: T): T {
        const ret = Array.prototype.reduceRight.call(callbackfn, initialValue);
        return ret;
    }*/

    private _flat(flatDepth = 1) {
        return this._flatResolve(this._another, flatDepth);
    }

    private _flatResolve(arr, flatDepth = 1) {
        //recursion break
        if (flatDepth == 0) {
            return arr;
        }
        let res = [];

        let reFlat = item => {
            item = Array.isArray(item) ? item : [item];
            let mapped = this._flatResolve(item, flatDepth - 1);
            res = res.concat(mapped);
        };
        arr.forEach(reFlat)

        return new Es2019Array(...res);
    }

    private _flatMap(mapperFunction: Function): any {
        let res = this.map(item => mapperFunction(item));
        return this._flatResolve(res);
    }
}

//let _Es2019Array = function<T>(...data: T[]) {};

//let oldProto = Es2019Array.prototype;

export function _Es2019Array<T>(...data: T[]): Es2019Array_<T> {
    let ret = new Es2019Array_<T>(...data);
    let proxied = new Proxy<Es2019Array_<T>>(ret, {
        get(target: Es2019Array_<unknown>, p: string | symbol, receiver: any): any {
            if("symbol" == typeof p) {

                return target._another[p];
            }
            if(!isNaN(parseInt(p as string))) {
                return target._another[p];
            } else {
                return target[p];
            }
        },

        set(target, property, value): boolean {
            target[property] = value;
            target._another[property] = value;
            return true;
        }

    });
    return proxied;
};

/**
 * this is the switch between normal array and our shim
 * the shim is only provided in case the native browser
 * does not yet have flatMap support on arrays
 */
export var Es2019Array: any = (Array.prototype.flatMap) ? function<T>(...data: T[]): T[] {
    return data;
} : _Es2019Array;