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

package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
public class VirtualMachine extends Model {
    @Id
    public String vmId;
    public String vmStatus;
    public String mode;

    public VirtualMachine(String vmId, String vmStatus, String mode){
        this.vmId = vmId;
        this.vmStatus = vmStatus;
        this.mode = mode;
    }

    public static Finder<String, VirtualMachine> finder = new Finder<String, VirtualMachine>(
            String.class, VirtualMachine.class
    );

    public static List<VirtualMachine> all(){
        return finder.all();
    }

    public static VirtualMachine findVM(String vmId){
        return finder.where().eq("vmId", vmId).findUnique();
    }

    public static void createVM(VirtualMachine vm){
        vm.save();
    }

    public static void deleteVM(VirtualMachine vm){
        vm.delete();
    }

}
