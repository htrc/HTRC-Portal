/**
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express  or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.indiana.d2i.htrc.sloan.bean;

public class VMImageDetails {
    private String vmImageName;
    private String vmImageDescription;

    public VMImageDetails(String imageName, String imageDescription){
        this.vmImageName = imageName;
        this.vmImageDescription = imageDescription;
    }

    public String getVmImageName() {
        return vmImageName;
    }

    public void setVmImageName(String vmImageName) {
        this.vmImageName = vmImageName;
    }

    public String getVmImageDescription() {
        return vmImageDescription;
    }

    public void setVmImageDescription(String vmImageDescription) {
        this.vmImageDescription = vmImageDescription;
    }
}
