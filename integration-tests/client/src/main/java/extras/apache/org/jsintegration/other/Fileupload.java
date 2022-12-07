/*
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
package extras.apache.org.jsintegration.other;



import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

@Named
@RequestScoped
public class Fileupload {

    private Part uploaded;
    private Part uploaded2;


    private String msg = "";


    public Part getUploaded2() {
        return uploaded2;
    }

    public void setUploaded2(Part uploaded2) {
        this.uploaded2 = uploaded2;
    }

    public void setUploaded(Part uploadedFile) {
        this.uploaded = uploadedFile;
    }

    public Part getUploaded() {
        return uploaded;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void doUpload() {
        if(uploaded != null && uploaded2 != null) {
            msg = "success";
        }
    }
}
