package nus.iss.backend.dao;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UpdateDoctorNotesReq {
    private UUID intakeHistoryId;
    private String editedNote;
}
