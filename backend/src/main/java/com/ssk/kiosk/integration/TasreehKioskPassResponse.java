package com.ssk.kiosk.integration;

import java.util.List;

public record TasreehKioskPassResponse(
    TasreehPassData data,
    List<TasreehError> errors,
    String message,
    String status,
    Integer statusCode) {

  public record TasreehPassData(
      String company,
      String date,
      String emiratesIdNumber,
      String hostNameAndApproverDepartment,
      String idProof,
      String location,
      String nationality,
      String passValidityDate,
      String passportNumber,
      String photo,
      String purposeOfVisit,
      String specificLocation,
      String typeOfPass,
      String vehiclePassNumber,
      String visitorName,
      String visitorType) {
  }

  public record TasreehError(Integer errCode, String errMsg) {
  }
}
