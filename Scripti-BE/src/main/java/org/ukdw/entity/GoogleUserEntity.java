/**
 * Author: dendy
 * Date:27/09/2024
 * Time:14:39
 * Description:
 */

package org.ukdw.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "google_users")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GoogleUserEntity {

    @Id
    @Column(name = "google_id", length = 100, nullable = false)
    @Setter
    @Getter
    private String googleId;

    @Column(name = "google_name", length = 128, nullable = false)
    @Setter
    @Getter
    private String googleName;

    @Column(name = "google_email", length = 128, nullable = false)
    @Setter
    @Getter
    private String googleEmail;

    @Column(name = "google_link", length = 256, nullable = false)
    @Setter
    @Getter
    private String googleLink;

    @Column(name = "google_picture_link", length = 512, nullable = false)
    @Setter
    @Getter
    private String googlePictureLink;

    @Column(name = "access_token", length = 256)
    @Setter
    @Getter
    private String accessToken;

    @Column(name = "id_token", length = 256)
    @Setter
    @Getter
    private String idToken;

    /*The refresh_token is only returned on the first request.
     When you refresh the access token a second time it returns everything except the refresh_token and the file_put_contents
      removes the refresh_token when this happens the second time.
      You will get refreshToken if user access to google API already revoked. */
    @Column(name = "refresh_token", length = 256)
    @Setter
    @Getter
    private String refreshToken;
}

