import {Config} from "mona-dish";



export class Context {
    P_PARTIAL_SOURCE?: string;
    PARTIAL_ID?: string;
    P_WINDOW_ID?:string;
    VIEW_ID?: string;
    P_VIEWSTATE?: string;
    P_CLIENT_WINDOW?: string;
    SOURCE: string;
    CTX_PARAM_SRC_FRM_ID?: string;
    CTX_PARAM_SRC_CTL_ID?: string;
    ON_EVENT ?: (HTMLEvent) => boolean | void;
    ON_ERROR ?: (HTMLEvent) => boolean | void;
    MYFACES ?: Config;
    CTX_PARAM_REQ_PASS_THR = {};
}
