import {Config, DomQuery, DQ, Es2019Array} from "mona-dish";
import {ExtDomQuery} from "./ExtDomQuery";
import {$faces, EMPTY_STR} from "../core/Const";

/*
 * various routines for encoding and decoding url parameters
 * into configs and vice versa
 */


/**
 * encodes a given form data into a url encoded string
 * @param formData the form data config object
 * @param paramsMapper the params mapper
 * @param defaultStr a default string if nothing comes out of it
 */
export function encodeFormData(formData: Config,
                               paramsMapper = (inStr, inVal) => [inStr, inVal],
                               defaultStr = EMPTY_STR): string {
    if (formData.isAbsent()) {
        return defaultStr;
    }
    const assocValues = formData.value;

    const expandValueArrAndRename = key => assocValues[key].map(val => paramsMapper(key, val));
    const isPropertyKey = key => assocValues.hasOwnProperty(key);
    const isNotFile = ([, value]) => !(value instanceof ExtDomQuery.global().File);
    const mapIntoUrlParam = keyVal => `${encodeURIComponent(keyVal[0])}=${encodeURIComponent(keyVal[1])}`;

    return new Es2019Array(...Object.keys(assocValues))
        .filter(isPropertyKey)
        .flatMap(expandValueArrAndRename)
        .filter(isNotFile)
        .map(mapIntoUrlParam)
        .join("&");
}

/**
 * splits and decodes encoded values into strings containing of key=value
 * @param encoded encoded string
 */
export function decodeEncodedValues(encoded: string): string[][] {
    const filterBlanks = item => !!(item || '').replace(/\s+/g, '');
    const splitKeyValuePair = _line => {
        let line = decodeURIComponent(_line);
        let index = line.indexOf("=");
        if (index == -1) {
            return [line];
        }
        return [line.substring(0, index), line.substring(index + 1)];
    };

    let requestParamEntries = encoded.split(/&/gi);
    return requestParamEntries.filter(filterBlanks).map(splitKeyValuePair);
}


/**
 * gets all the input files and their corresponding file objects
 * @param dataSource
 */
export function resolveFiles(dataSource: DQ): [string, File][] {

    const expandFilesArr = ([key, files]) => {
        return [...files].map(file => [key, file]);
    }
    const remapFileInput = fileInput => {
        return [fileInput.name.value || fileInput.id.value, fileInput.filesFromElem(0)];
    }

    const files = dataSource
        .querySelectorAllDeep("input[type='file']")
        .asArray;

    const ret = files
        .map(remapFileInput)
        .flatMap(expandFilesArr);

    return ret as any;
}


export function fixEmptyParameters(keyVal: any[]): [string, any] {
    return (keyVal.length < 3 ? [keyVal?.[0] ?? [], keyVal?.[1] ?? []] : keyVal) as [string, any];
}

/**
 * returns the decoded viewState from parentItem
 * @param parentItem
 */
function resolveViewState(parentItem: DomQuery): string[][] | [string, File][] {
    const viewStateStr = $faces().getViewState(parentItem.getAsElem(0).value);

    // we now need to decode it and then merge it into the target buf
    // which hosts already our overrides (aka do not override what is already there(
    // after that we need to deal with form elements on a separate level
    return decodeEncodedValues(viewStateStr);
}

/**
 * gets all the inputs under the form parentItem
 * as array
 * @param parentItem
 */
export function getFormInputsAsArr(parentItem: DomQuery): string[][] | [string, File][] {
    const standardInputs: any = resolveViewState(parentItem);
    const fileInputs = resolveFiles(parentItem);
    return standardInputs.concat(fileInputs)
}