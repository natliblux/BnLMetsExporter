/*******************************************************************************
 * Copyright (C) 2021 Bibliothèque nationale de Luxembourg (BnL)
 *
 * This file is part of BnLMetsExporter.
 *
 * BnLMetsExporter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BnLMetsExporter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BnLMetsExporter.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lu.bnl.domain.model.marc;

import java.util.List;

// Code from Keymaps-service
public class MarcRecordDTO {
    
    private String leader;

    private List<MarcControlFieldDTO> controlFields;

    private List<MarcDataFieldDTO> dataFields;

    public MarcRecordDTO() {

    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public List<MarcControlFieldDTO> getControlFields() {
        return controlFields;
    }

    public void setControlFields(List<MarcControlFieldDTO> controlFields) {
        this.controlFields = controlFields;
    }

    public List<MarcDataFieldDTO> getDataFields() {
        return dataFields;
    }

    public void setDataFields(List<MarcDataFieldDTO> dataFields) {
        this.dataFields = dataFields;
    }

}
