// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: counter.proto

package com.hyf.jraft.counter.rpc;

public final class Counter {
  private Counter() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_GetValueRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_GetValueRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_IncrementAndGetRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_IncrementAndGetRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ValueResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ValueResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rcounter.proto\"\021\n\017GetValueRequest\"\'\n\026In" +
      "crementAndGetRequest\022\r\n\005count\030\001 \001(\003\"_\n\rV" +
      "alueResponse\022\017\n\007success\030\001 \001(\010\022\014\n\004code\030\002 " +
      "\001(\005\022\020\n\010redirect\030\003 \001(\t\022\016\n\006errMsg\030\004 \001(\t\022\r\n" +
      "\005count\030\005 \001(\003B\035\n\031com.hyf.jraft.counter.rp" +
      "cP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_GetValueRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_GetValueRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_GetValueRequest_descriptor,
        new java.lang.String[] { });
    internal_static_IncrementAndGetRequest_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_IncrementAndGetRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_IncrementAndGetRequest_descriptor,
        new java.lang.String[] { "Count", });
    internal_static_ValueResponse_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_ValueResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ValueResponse_descriptor,
        new java.lang.String[] { "Success", "Code", "Redirect", "ErrMsg", "Count", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
