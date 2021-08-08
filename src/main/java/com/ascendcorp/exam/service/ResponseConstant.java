package com.ascendcorp.exam.service;

public class ResponseConstant {


    public enum ResponseCode {
        APPROVED("200"),
        INVALID_DATA("400"),
        TRANSACTION_ERROR("500"),
        UNKNOWN("501");

        private String value;
        /**
         * Get value of search type.
         */
        private ResponseCode(String c) {
            value = c;
        }

        /**
         * Get value of search type.
         *
         * @return search type
         */
        public String getValue() {
            return value;
        }
    }
    public enum Error {
        INVALID_DATA("500", "General Invalid Data"),
        TIMEOUT("503", "Error timeout"),
        INTERNAL_APPLICATION("504", "Internal Application Error");

        private final String code;
        private final String description;

        private Error(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code + ": " + description;
        }
    }

}
