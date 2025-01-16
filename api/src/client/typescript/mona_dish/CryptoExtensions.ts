/*!
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import {Crypto, Hash} from "./Messaging";
import {AssocArrayCollector} from "./SourcesCollectors";
import {LazyStream} from "./Stream";

/*
 * Some crypto implementations which might come in handy
 */


/**
 * basic json stringify encryption impl
 * this does not really full encryption except for a standard json stringyfywith an encapsulation json
 *
 * the return value resembles:
 * <pre>
 *     {
 *         encryptedData: <data as string>
 *     }
 * </pre>
 */
export class JSONCrypto implements Crypto {
    decode(data: any): any {
        if (data?.encryptedData) {
            return JSON.parse(data.encryptedData);
        }
        return data;
    }

    encode(data: any) {
        return {
            encryptedData: JSON.stringify(data)
        }
    }
}


/**
 * a class with  timeout functionality which blocks decodes after a certain period of time
 * if the message is not decoded by then
 * We use hash as identifier generation after encryption to make sure
 * a trace was possible
 *
 * The idea behind this is to have a generic wrapper which allows messages with dynamic encryption
 * where keys/salts only exist for a certain period of time before expiring!
 * That way someone who implements such a scheme does not have to take care about the bookeeping mechanisms!
 * Or you can use crypto mechanisms which do not have expiring keys and still expire them automatically
 *
 * I will leave it up to the system integrator to provide a rotating crypto class, because this is highly
 * implementation dependent. But it helps to have a wrapper!
 */
export class ExpiringCrypto implements Crypto {

    private static MAX_GC_CYCLES = 10;
    private gcCycleCnt = 0;
    private storedMessages: { [key: string]: number } = {};
    private lastCall = 0;

    /**
     * @param timeout timeout in milliseconds until a message is expired
     * @param parentCrypto the embedded decorated crypto algorithm
     * @param hashSum hashshum implementation to generate a hash
     */
    constructor(private timeout: number, private parentCrypto: Crypto, private hashSum: Hash) {

    }

    /**
     * decode implementation with a timeout hook install
     * @param data
     */
    decode(data: any): any {
        //if ((this.gcCycleCnt++ % ExpiringCrypto.MAX_GC_CYCLES) === 0) {

        const currTime = new Date().getTime();
        if(this.gcLimitReached(currTime)) {
            this.storedMessages = LazyStream
                .ofAssoc(this.storedMessages)
                .filter(data => data[1] >= currTime)
                .collect(new AssocArrayCollector());
        }
        this.lastCall = currTime;


        let rotatingEncoded = this.hashSum.encode(data);
        if (!this.storedMessages?.[rotatingEncoded.toString()]) {
            throw Error("An item was tried to be decryted which either was expired or invalid");
        }
        return this.parentCrypto.decode(data);
    }

    /**
     * trigger function to determine whether the gc needs to cycle again, this is either time or call based
     * the gc itself collects only on expiration dates
     * The idea is to run this operation only occasionally because it is costly
     * We also could have used timeouts etc.. but those would need shutdown/destroy cleanups
     *
     * @param currTime
     * @private
     */
    private gcLimitReached(currTime: number) {
        return (this.lastCall + this.timeout) < currTime || ((++this.gcCycleCnt) % ExpiringCrypto.MAX_GC_CYCLES == 0);
    }

    /**
     * encode with a timeout hook installed
     * calls the encode of the delegated object
     *
     * @param data
     */
    encode(data: any): any {
        let encoded = this.parentCrypto.encode(data);
        //ok use the hashsum really only to store expirations, theoretically there could be a second message which does not invalidate the first one
        //but this is very unlikely unless a message is sent over and over again, in this case we have a timeout extension anyway!
        let rotatingEncoded = this.hashSum.encode(encoded);
        this.storedMessages[rotatingEncoded.toString()] = (new Date().getTime()) + this.timeout;
        return encoded;
    }
}