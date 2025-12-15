/**
 * Author: dendy
 * Date:30/09/2024
 * Time:7:47
 * Description: Error custom response
 */

package org.ukdw.exception;

import lombok.Data;

@Data
public class ErrorResponse {
    private String status;
    private String message;

    public ErrorResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

}
