// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: counter.proto

package com.hyf.jraft.counter.rpc;

public interface ValueResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ValueResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bool success = 1;</code>
   * @return The success.
   */
  boolean getSuccess();

  /**
   * <code>int32 code = 2;</code>
   * @return The code.
   */
  int getCode();

  /**
   * <code>string redirect = 3;</code>
   * @return The redirect.
   */
  java.lang.String getRedirect();
  /**
   * <code>string redirect = 3;</code>
   * @return The bytes for redirect.
   */
  com.google.protobuf.ByteString
      getRedirectBytes();

  /**
   * <code>string errMsg = 4;</code>
   * @return The errMsg.
   */
  java.lang.String getErrMsg();
  /**
   * <code>string errMsg = 4;</code>
   * @return The bytes for errMsg.
   */
  com.google.protobuf.ByteString
      getErrMsgBytes();

  /**
   * <code>int64 count = 5;</code>
   * @return The count.
   */
  long getCount();
}