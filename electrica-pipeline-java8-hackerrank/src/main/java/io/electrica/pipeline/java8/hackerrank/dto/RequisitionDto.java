package io.electrica.pipeline.java8.hackerrank.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = RequisitionDto.ROOT_ELEMENT_NAME)
public class RequisitionDto {

    public static final String ROOT_ELEMENT_NAME = "REQUISITION";
    public static final String ROOT_TAG = "<" + ROOT_ELEMENT_NAME + ">";
}
