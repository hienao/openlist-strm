package com.hienao.openlist2strm.dto.sign;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDto {
  @NotEmpty private String oldPassword;

  @NotEmpty private String newPassword;
}
