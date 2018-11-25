package com.project.utility;

import org.apache.commons.codec.digest.DigestUtils;

public class FileHasher {
  
  public FileHasher() {}

  public static String getMD5Hash(String content) {
    return DigestUtils.md5Hex(content);
  }

  public static String getSHA256Hash(String content) {
    return DigestUtils.sha256Hex(content);
  }
}