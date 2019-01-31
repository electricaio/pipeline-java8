package io.electrica.pipeline.java8.hackerrank.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = CandidateDto.ROOT_ELEMENT_NAME)
public class CandidateDto {

    public static final String ROOT_ELEMENT_NAME = "CANDIDATE";
    public static final String ROOT_TAG = "<" + ROOT_ELEMENT_NAME + ">";

    //<CANDIDATEID>
//<REQUISITIONNUMBER>
//<BRREQNUMBER>
//<JOBCODE>
//<STATUS>
//<CANDIDATEMAIL>
//<CANDIDATEFIRSTNAME>
//<CANDIDATELASTNAME>
}
