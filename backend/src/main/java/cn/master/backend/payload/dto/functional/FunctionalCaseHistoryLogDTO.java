package cn.master.backend.payload.dto.functional;

import cn.master.backend.entity.FileAssociation;
import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.entity.FunctionalCaseAttachment;
import cn.master.backend.entity.FunctionalCaseCustomField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/10/2024
 **/
@Data
public class FunctionalCaseHistoryLogDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private FunctionalCase functionalCase;

    //private FunctionalCaseBlob functionalCaseBlob;

    private List<FunctionalCaseCustomField> customFields;

    private List<FunctionalCaseAttachment> caseAttachments;

    private List<FileAssociation> fileAssociationList;

    public FunctionalCaseHistoryLogDTO(FunctionalCase functionalCase, List<FunctionalCaseCustomField> customFields, List<FunctionalCaseAttachment> caseAttachments, List<FileAssociation> fileAssociationList) {
        this.functionalCase = functionalCase;
        //this.functionalCaseBlob = functionalCaseBlob;
        this.customFields = customFields;
        this.caseAttachments = caseAttachments;
        this.fileAssociationList = fileAssociationList;
    }

    public FunctionalCaseHistoryLogDTO() {
    }
}
