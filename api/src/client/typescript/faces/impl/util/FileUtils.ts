import {ArrayCollector, Config, DomQuery, DQ, LazyStream, Stream} from "mona-dish";
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

    const expandValueArrAndRename = key => Stream.of(...assocValues[key]).map(val => paramsMapper(key, val));
    const isPropertyKey = key => assocValues.hasOwnProperty(key);
    const isNotFile = ([, value]) => !(value instanceof ExtDomQuery.global().File);
    const mapIntoUrlParam = keyVal => `${encodeURIComponent(keyVal[0])}=${encodeURIComponent(keyVal[1])}`;

    const entries = LazyStream.of(...Object.keys(assocValues))
        .filter(isPropertyKey)
        .flatMap(expandValueArrAndRename)
        //we cannot encode file elements that is handled by multipart requests anyway
        .filter(isNotFile)
        .map(mapIntoUrlParam)
        .collect(new ArrayCollector());

    return entries.join("&")
}

/**
 * splits and decodes encoded values into strings containing of key=value
 * @param encoded encoded string
 */
export function decodeEncodedValues(encoded: string): Stream<string[]> {
    const filterBlanks = item => !!(item || '').replace(/\s+/g, '');
    const splitKeyValuePair = line => {
        let index = line.indexOf("=");
        if (index == -1) {
            return [line];
        }
        return [line.substring(0, index), line.substring(index + 1)];
    };

    let requestParamEntries = decodeURIComponent(encoded).split(/&/gi);
    return Stream.of(...requestParamEntries)
        .filter(filterBlanks)
        .map(splitKeyValuePair)
}


/**
 * gets all the input files and their corresponding file objects
 * @param dataSource
 */
export function resolveFiles(dataSource: DQ): Stream<[string, File]> {

    const expandFilesArr = ([key, files]) => Stream.of(...files).map(file => [key, file]);
    const remapFileInput = fileInput => [fileInput.name.value || fileInput.id.value, fileInput.filesFromElem(0)];
    return dataSource
        .querySelectorAllDeep("input[type='file']")
        .stream
        .map(remapFileInput)
        .flatMap(expandFilesArr);
}


export function fixKeyWithoutVal(keyVal: any[]): [string, any] {
    return (keyVal.length < 3 ? [keyVal?.[0] ?? [], keyVal?.[1] ?? []] : keyVal) as [string, any];
}

/**
 * returns the decoded viewState from parentItem
 * @param parentItem
 */
function resolveViewState(parentItem: DomQuery): Stream<string[] | [string, File]> {
    const viewStateStr = $faces().getViewState(parentItem.getAsElem(0).value);

    // we now need to decode it and then merge it into the target buf
    // which hosts already our overrides (aka do not override what is already there(
    // after that we need to deal with form elements on a separate level
    return decodeEncodedValues(viewStateStr);
}

/**
 * gets all the inputs under the form parentItem
 * as stream
 * @param parentItem
 */
export function getFormInputsAsStream(parentItem: DomQuery): Stream<string[] | [string, File]> {
    const standardInputs = resolveViewState(parentItem);
    const fileInputs = resolveFiles(parentItem);
    return  standardInputs.concat(fileInputs as any)
}