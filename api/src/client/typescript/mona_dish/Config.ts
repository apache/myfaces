import {Es2019Array} from "./Es2019Array";
import {IValueHolder, Optional, ValueEmbedder} from "./Monad";
import {Lang} from "./Lang";
const objAssign = Lang.objAssign;
import {append, appendIf, assign, assignIf, resolve, shallowMerge} from "./AssocArray";

/**
 * specialized value embedder
 * for our Configuration
 */
class ConfigEntry<T> extends ValueEmbedder<T> {

    /*default value for absent*/
    static absent = ConfigEntry.fromNullable(null);

    /**
     * arrayed value positions
     */
    arrPos: number;

    constructor(rootElem: any, key: any, arrPos?: number) {
        super(rootElem, key);

        this.arrPos = arrPos ?? -1;
    }

    get value() {
        if (this.key == "" && this.arrPos >= 0) {
            return this._value[this.arrPos];
        } else if (this.key && this.arrPos >= 0) {
            return this._value[this.key][this.arrPos];
        }
        return this._value[this.key];
    }

    set value(val: T) {
        if (this.key == "" && this.arrPos >= 0) {
            this._value[this.arrPos] = val;
            return;
        } else if (this.key && this.arrPos >= 0) {
            this._value[this.key][this.arrPos] = val;
            return;
        }
        this._value[this.key] = val;
    }
}

export const CONFIG_VALUE = "__END_POINT__";
export const CONFIG_ANY = "__ANY_POINT__";
export type ConfigDef = { [key: string]: any };

/**
 * Config, basically an optional wrapper for a json structure
 * (not Side - effect free, since we can alter the internal config state
 * without generating a new config), not sure if we should make it side - effect free
 * since this would swallow a lot of performance and ram
 */
export class Config extends Optional<any> {
    constructor(root: any, private configDef ?: ConfigDef) {
        super(root);
    }

    /**
     * shallow copy getter, copies only the first level, references the deeper nodes
     * in a shared manner
     */
    get shallowCopy(): Config {
        return this.shallowCopy$();
    }

    protected shallowCopy$(): Config {
        let ret = new Config({});
        ret.shallowMerge(this.value);
        return ret;
    }

    /**
     * deep copy, copies all config nodes
     */
    get deepCopy(): Config {
        return this.deepCopy$();
    }

    protected deepCopy$(): Config {
        return new Config(objAssign({}, this.value));
    }

    /**
     * creates a config from an initial value or null
     * @param value
     */
    static fromNullable<T>(value?: T | null): Config {
        return new Config(value);
    }

    /**
     * simple merge for the root configs
     */
    shallowMerge(other: Config, overwrite = true, withAppend = false) {
        //shallow merge must be mutable so we have to remap
        let newThis = shallowMerge(overwrite, withAppend, this.value, other.value);
        if (Array.isArray(this._value)) {
            this._value.length = 0;
            this._value.push(...(newThis as any));
        } else {
            Object.getOwnPropertyNames(this._value).forEach(key => delete this._value[key]);
            Object.getOwnPropertyNames(newThis).forEach(key => this._value[key] = newThis[key]);
        }
    }

    /**
     * assigns a single value as array, or appends it
     * to an existing value mapping a single value to array
     *
     *
     * usage myConfig.append("foobaz").value = "newValue"
     *       myConfig.append("foobaz").value = "newValue2"
     *
     * resulting in myConfig.foobaz == ["newValue, newValue2"]
     *
     * @param {string[]} accessPath
     */
    append(...accessPath: string[]): IValueHolder<any> {
        return append(this._value, ...accessPath);
    }

    /**
     * appends to an existing entry (or extends into an array and appends)
     * if the condition is met
     * @param {boolean} condition
     * @param {string[]} accessPath
     */
    appendIf(condition: boolean, ...accessPath: string[]): IValueHolder<any> {
        return appendIf(condition, this._value, ...accessPath);
    }

    /**
     * assigns a new value on the given access path
     * @param accessPath
     */
    assign(...accessPath): IValueHolder<any> {
        return assign(this.value, ...accessPath);
    }


    /**
     * assign a value if the condition is set to true, otherwise skip it
     *
     * @param condition the condition, the access accessPath into the config
     * @param accessPath
     */
    assignIf(condition: boolean, ...accessPath: Array<any>): IValueHolder<any> {
        return assignIf(condition, this._value, ...accessPath);
    }

    /**
     * get if the access path is present (get is reserved as getter with a default, on the current path)
     * TODO will be renamed to something more meaningful and deprecated, the name is ambiguous
     * @param accessPath the access path
     */
    getIf(...accessPath: Array<string>): Config {
        this.assertAccessPath(...accessPath);
        return this.getClass().fromNullable(resolve(this.value, ...accessPath));
    }


    /**
     * gets the current node and if none is present returns a config with a default value
     * @param defaultVal
     */
    get(defaultVal: any): Config {
        return this.getClass().fromNullable(super.get(defaultVal).value);
    }

    //empties the current config entry
    delete(key: string): Config {
        if (key in this.value) {
            delete this.value[key];
        }
        return this;
    }

    /**
     * converts the entire config into a json object
     */
    toJson(): any {
        return JSON.stringify(this.value);
    }


    protected getClass(): any {
        return Config;
    }

    private setVal(val: any) {
        this._value = val;
    }


    /**
     * asserts the access path for a semi typed access
     * @param accessPath
     * @private
     */
    private assertAccessPath(...accessPath: Array<string>) {
        accessPath = this.preprocessKeys(...accessPath);
        if (!this.configDef) {
            //untyped
            return;
        }

        const ERR_ACCESS_PATH = "Access Path to config invalid";
        let currAccessPos: any = Optional.fromNullable(Object.keys(this.configDef).map(key => {
            let ret = {};
            ret[key] = this.configDef[key];
            return ret;
        }));

        for (let cnt = 0; cnt < accessPath.length; cnt++) {
            let currKey = this.keyVal(accessPath[cnt]);
            let arrPos: any = this.arrayIndex(accessPath[cnt]);

            //key index
            if (this.isArray(arrPos)) {
                if (currKey != "") {
                    currAccessPos = Array.isArray(currAccessPos.value) ?
                        Optional.fromNullable(new Es2019Array(...currAccessPos.value)
                            .find(item => {
                                return !!(item?.[currKey] ?? false)
                            })?.[currKey]?.[arrPos]) :
                        Optional.fromNullable(currAccessPos.value?.[currKey]?.[arrPos] ?? null);

                } else {
                    currAccessPos = (Array.isArray(currAccessPos.value)) ?
                        Optional.fromNullable(currAccessPos.value?.[arrPos]) : Optional.absent;
                }
                //we noe store either the current array or the filtered look ahead to go further
            } else {
                //we now have an array and go further with a singular key
                currAccessPos = (Array.isArray(currAccessPos.value)) ? Optional.fromNullable(new Es2019Array(...currAccessPos.value)
                        .find(item => {
                            return !!(item?.[currKey] ?? false);
                        })?.[currKey]) :
                    Optional.fromNullable(currAccessPos.value?.[currKey] ?? null);
            }
            if (!currAccessPos.isPresent()) {
                throw Error(ERR_ACCESS_PATH)
            }
            if (currAccessPos.value == CONFIG_ANY) {
                return;
            }
        }
    }

    private isNoArray(arrPos: number) {
        return arrPos == -1;
    }

    private isArray(arrPos: number) {
        return !this.isNoArray(arrPos);
    }

}