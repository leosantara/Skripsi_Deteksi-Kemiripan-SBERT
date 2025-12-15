package org.ukdw.dto;

import lombok.Data;
import org.ukdw.entity.GoogleUserEntity;

import java.util.ArrayList;
import java.util.Set;

@Data
public class UserDTO {


    String username;

    String password;

    ArrayList<GroupDTO> groups;

    String fullname;

    // Foreign key relationship with google_users.google_email
    GoogleUserEntity googleUser;

    String nim;

    String modified;

    String telpNo;

    Boolean active;

    String hashValidation;

    String foto;

    String dir;

    String mimetype;

    Integer fileSize;

    String hashtag;

    String jwtToken;
}
