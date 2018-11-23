package com.project.utility;

import org.apache.commons.codec.digest.DigestUtils;

public class FileHasher {
  
  public FileHasher() {}

  public String getMD5Hash(String text) {
    return DigestUtils.md5Hex(text);
  }

  public String getSHA256Hash(String text) {
    return DigestUtils.sha256Hex(text);
  }
}