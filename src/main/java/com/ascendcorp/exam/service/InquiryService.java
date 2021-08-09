package com.ascendcorp.exam.service;

import com.ascendcorp.exam.model.InquiryServiceResultDTO;
import com.ascendcorp.exam.model.TransferResponse;
import com.ascendcorp.exam.proxy.BankProxyGateway;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;

import javax.xml.ws.WebServiceException;
import java.text.MessageFormat;
import java.util.Date;

public class InquiryService {

    @Autowired
    private BankProxyGateway bankProxyGateway;

    final static Logger log = Logger.getLogger(InquiryService.class);
    private static final String MSG_ERROR_REQUIRED = "msg.error.required";
    private static final String MSG_GENERAL_INVALID_DATA = "msg.error.general_invalid_data";

    /**
     * Validate request parameters before inquiry.
     *
     * @param transactionId - the value of transactionId
     * @param tranDateTime  - the value of tranDateTime
     * @param channel       - the value of channel
     * @param bankCode      - the value of bankCode
     * @param bankNumber    - the value of bankNumber
     * @param amount        - the value of amount
     * @throws NullPointerException if the validation fail.
     */
    private void validateBeforeRequest(String transactionId,
                                      Date tranDateTime,
                                      String channel,
                                      String bankCode,
                                      String bankNumber,
                                      double amount) {
        String errorMessage = null;
        if (StringUtils.isBlank(transactionId)) {
            errorMessage = MessageFormat.format(Utils.getMessagesProperties(MSG_ERROR_REQUIRED), "Transaction id");
        }
        if (tranDateTime == null) {
            errorMessage = MessageFormat.format(Utils.getMessagesProperties(MSG_ERROR_REQUIRED), "Transaction DateTime");
        }
        if (StringUtils.isBlank(channel)) {
            errorMessage = MessageFormat.format(Utils.getMessagesProperties(MSG_ERROR_REQUIRED), "Channel");
        }
        if (StringUtils.isBlank(bankCode)) {
            errorMessage = MessageFormat.format(Utils.getMessagesProperties(MSG_ERROR_REQUIRED), "Bank Code");
        }
        if (StringUtils.isBlank(bankNumber)) {
            errorMessage = MessageFormat.format(Utils.getMessagesProperties(MSG_ERROR_REQUIRED), "Bank Number");
        }
        if (amount <= 0) {
            errorMessage = Utils.getMessagesProperties("msg.error.amount_must_more_than_zero");
        }

        if (errorMessage != null) {
            log.info(errorMessage);
            throw new NullPointerException(errorMessage);
        }

    }

    /**
     * The service for inquiry information from bank by bank account.
     *
     * @param transactionId - the value of transactionId
     * @param tranDateTime  - the value of tranDateTime
     * @param channel       - the value of channel
     * @param locationCode  - the value of locationCode
     * @param bankCode      - the value of bankCode
     * @param bankNumber    - the value of bankNumber
     * @param amount        - the value of amount
     * @param reference1    - the value of reference1
     * @param reference2    - the value of reference2
     * @param firstName     - the value of firstName
     * @param lastName      - the value of lastName
     * @return response from inquiry.
     */
    public InquiryServiceResultDTO inquiry(String transactionId,
                                           Date tranDateTime,
                                           String channel,
                                           String locationCode,
                                           String bankCode,
                                           String bankNumber,
                                           double amount,
                                           String reference1,
                                           String reference2,
                                           String firstName,
                                           String lastName) {

        InquiryServiceResultDTO respDTO = new InquiryServiceResultDTO();
        try {
            log.info("validate request parameters.");
            validateBeforeRequest(transactionId, tranDateTime, channel, bankCode, bankNumber, amount);

            log.info("call bank web service");
            TransferResponse response = bankProxyGateway.requestTransfer(transactionId, tranDateTime, channel,
                    bankCode, bankNumber, amount, reference1, reference2);

            log.info("check bank response code");
            respDTO = transformResponse(response);

        } catch (NullPointerException ne) {

            respDTO = new InquiryServiceResultDTO();
            respDTO.setReasonCode(ResponseConstant.Error.INVALID_DATA.getCode());
            respDTO.setReasonDesc(ResponseConstant.Error.INVALID_DATA.getDescription());
        } catch (WebServiceException r) {

            // handle error from bank web service
            String faultString = r.getMessage();

            if (faultString != null && (faultString.indexOf("java.net.SocketTimeoutException") > -1
                    || faultString.indexOf("Connection timed out") > -1)) {
                // bank timeout
                respDTO.setReasonCode(ResponseConstant.Error.TIMEOUT.getCode());
                respDTO.setReasonDesc(ResponseConstant.Error.TIMEOUT.getDescription());
            } else {
                // bank general error
                respDTO.setReasonCode(ResponseConstant.Error.INTERNAL_APPLICATION.getCode());
                respDTO.setReasonDesc(ResponseConstant.Error.INTERNAL_APPLICATION.getDescription());
            }
        } catch (Exception e) {

            log.error("inquiry exception", e);
            respDTO.setReasonCode(ResponseConstant.Error.INTERNAL_APPLICATION.getCode());
            respDTO.setReasonDesc(ResponseConstant.Error.INTERNAL_APPLICATION.getDescription());

        }
        return respDTO;
    }

    /**
     * Transform response data from inquiry service.
     *
     * @param response - the response data from inquiry.
     * @return result.
     * @throws Exception if response is null or responseCode not support.
     */
    public InquiryServiceResultDTO transformResponse(TransferResponse response) throws Exception {
        InquiryServiceResultDTO inquiryResult = new InquiryServiceResultDTO();
        if (response != null) //New
        {
            log.debug("found response code");

            inquiryResult.setRefNo1(response.getReferenceCode1());
            inquiryResult.setRefNo2(response.getReferenceCode2());
            inquiryResult.setAmount(response.getBalance());
            inquiryResult.setTranID(response.getBankTransactionID());

            if (ResponseConstant.ResponseCode.APPROVED.toString().equalsIgnoreCase(response.getResponseCode())) {
                // bank response code = approved
                inquiryResult.setReasonCode(ResponseConstant.ResponseCode.APPROVED.getValue());
                inquiryResult.setReasonDesc(response.getDescription());
                inquiryResult.setAccountName(response.getDescription());

            } else if (ResponseConstant.ResponseCode.INVALID_DATA.toString().equalsIgnoreCase(response.getResponseCode())) {
                // bank response code = invalid_data
                inquiryResult = transformResponseInvalidData(response.getDescription());

            } else if (ResponseConstant.ResponseCode.TRANSACTION_ERROR.toString().equalsIgnoreCase(response.getResponseCode())) {
                // bank response code = transaction_error
                inquiryResult = transformResponseTransactionError(response.getDescription());
            } else if (ResponseConstant.ResponseCode.UNKNOWN.toString().equalsIgnoreCase(response.getResponseCode())) {
                inquiryResult = transformResponseUnknown(response.getDescription());
            } else {
                // bank code not support
                throw new Exception(Utils.getMessagesProperties("msg.error.unsupport_error_reason_code"));
            }
        } else {
            // no resport from bank
            throw new Exception(Utils.getMessagesProperties("msg.error.unable.inquiry"));
        }
        return inquiryResult;
    }

    /**
     * Transform response invalid data.
     *
     * @param responseDesc - the description of response.
     * @return result.
     */
    private InquiryServiceResultDTO transformResponseInvalidData(String responseDesc) {
        InquiryServiceResultDTO inquiryResult = new InquiryServiceResultDTO();

        if (responseDesc != null) {
            String[] splitDesc = responseDesc.split(":");
            if (splitDesc != null && splitDesc.length >= 3) {
                // bank description full format
                inquiryResult.setReasonCode(splitDesc[1]);
                inquiryResult.setReasonDesc(splitDesc[2]);
            } else {
                // bank description short format
                inquiryResult.setReasonCode(ResponseConstant.ResponseCode.INVALID_DATA.getValue());
                inquiryResult.setReasonDesc(Utils.getMessagesProperties(MSG_GENERAL_INVALID_DATA));
            }
        } else {
            // bank no description
            inquiryResult.setReasonCode(ResponseConstant.ResponseCode.INVALID_DATA.getValue());
            inquiryResult.setReasonDesc(Utils.getMessagesProperties(MSG_GENERAL_INVALID_DATA));
        }
        return inquiryResult;
    }

    /**
     * Transform response transaction error.
     *
     * @param responseDesc - the description of response.
     * @return result.
     */
    private InquiryServiceResultDTO transformResponseTransactionError(String responseDesc) {
        InquiryServiceResultDTO inquiryResult = new InquiryServiceResultDTO();

        if (responseDesc != null) {
            String[] splitDesc = responseDesc.split(":");
            if (splitDesc != null && splitDesc.length >= 2) {
                log.info("Case Inquiry Error Code Format Now Will Get From [0] and [1] first");
                String subIdx1 = splitDesc[0];
                String subIdx2 = splitDesc[1];
                log.info("index[0] : " + subIdx1 + " index[1] is >> " + subIdx2);
                if ("98".equalsIgnoreCase(subIdx1)) {
                    // bank code 98
                    inquiryResult.setReasonCode(subIdx1);
                    inquiryResult.setReasonDesc(subIdx2);
                } else {
                    log.info("case error is not 98 code");
                    if (splitDesc.length >= 3) {
                        // bank description full format
                        String subIdx3 = splitDesc[2];
                        log.info("index[0] : " + subIdx3);
                        inquiryResult.setReasonCode(subIdx2);
                        inquiryResult.setReasonDesc(subIdx3);
                    } else {
                        // bank description short format
                        inquiryResult.setReasonCode(subIdx1);
                        inquiryResult.setReasonDesc(subIdx2);
                    }
                }
            } else {
                // bank description incorrect format
                inquiryResult.setReasonCode(ResponseConstant.ResponseCode.TRANSACTION_ERROR.getValue());
                inquiryResult.setReasonDesc(Utils.getMessagesProperties("msg.error.general_transaction_error"));
            }
        } else {
            // bank no description
            inquiryResult.setReasonCode(ResponseConstant.ResponseCode.TRANSACTION_ERROR.getValue());
            inquiryResult.setReasonDesc(Utils.getMessagesProperties("msg.error.general_transaction_error"));
        }
        return inquiryResult;
    }

    /**
     * Transform response unknown.
     *
     * @param responseDesc - the description of response.
     * @return result.
     */
    private InquiryServiceResultDTO transformResponseUnknown(String responseDesc) {
        InquiryServiceResultDTO inquiryResult = new InquiryServiceResultDTO();

        if (responseDesc != null) {
            String[] splitDesc = responseDesc.split(":");
            if (splitDesc != null && splitDesc.length >= 2) {
                // bank description full format
                inquiryResult.setReasonCode(splitDesc[0]);
                inquiryResult.setReasonDesc(splitDesc[1]);
                if (inquiryResult.getReasonDesc() == null || inquiryResult.getReasonDesc().trim().length() == 0) {
                    inquiryResult.setReasonDesc(Utils.getMessagesProperties(MSG_GENERAL_INVALID_DATA));
                }
            } else {
                // bank description short format
                inquiryResult.setReasonCode(ResponseConstant.ResponseCode.UNKNOWN.getValue());
                inquiryResult.setReasonDesc(Utils.getMessagesProperties(MSG_GENERAL_INVALID_DATA));
            }
        } else {
            // bank no description
            inquiryResult.setReasonCode(ResponseConstant.ResponseCode.UNKNOWN.getValue());
            inquiryResult.setReasonDesc(Utils.getMessagesProperties(MSG_GENERAL_INVALID_DATA));
        }
        return inquiryResult;
    }


}
