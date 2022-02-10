/*
 * Copyright (c) 2022 WalluM-Systems UG (haftungsbeschränkt)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.wallumsystems.sas.tools;

import com.wallumsystems.sas.entity.AccountEntity;
import com.wallumsystems.sas.entity.RecordEntity;
import com.wallumsystems.sas.entity.TaxRecordEntity;
import com.wallumsystems.sas.swagger.model.Account;
import com.wallumsystems.sas.swagger.model.NewRecord;
import com.wallumsystems.sas.swagger.model.Record;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.ZoneOffset;

public class EntityToComponentConverter {

    private EntityToComponentConverter() {
    }

    public static Record recordEntityToRecord(RecordEntity recordEntity) {
        // TODO: check for the narrowing conversion
        return new Record()
                .id(recordEntity.getId().intValue())
                .description(recordEntity.getDescription())
                .from(accountEntityToAccount(recordEntity.getFromAccountEntity()))
                .to(accountEntityToAccount(recordEntity.getToAccountEntity()))
                .taxRecord(recordEntity.getTaxRecord().getId().intValue())
                .revertingRecord(recordEntity.getRevertingRecord().getId().intValue())
                .amount(BigDecimal.valueOf(recordEntity.getValue()))
                .bookingDate(recordEntity.getBookingDate().toLocalDate())
                .creationTime(recordEntity.getCreationTime().toInstant().atOffset(ZoneOffset.UTC));
    }

    public static RecordEntity newRecordToRecordEntity(NewRecord newRecord) {
        // TODO: insert the missing fields and check for the relations to other entities
        return RecordEntity.builder()
                .description(newRecord.getDescription())
                .bookingDate(Date.valueOf(newRecord.getBookingDate()))
                .fromAccountEntity(null)
                .toAccountEntity(null)
                .taxRecord(recordToTaxRecordEntity(newRecord.getTaxRecord()))
                .value(newRecord.getAmount().doubleValue())
                .bookingDate(Date.valueOf(newRecord.getBookingDate()))
                .build();
    }

    public static TaxRecordEntity recordToTaxRecordEntity(Record recordToConvert) {
        // TODO: insert the missing fields and check for the relations to other entities
        // TODO: the whole handling of this situation is not elegant.
        //  Maybe a service is needed to have the possibility to access repositories and work this whole thing out.
        return recordToConvert == null ? null : TaxRecordEntity.builder()
                .description(recordToConvert.getDescription())
                .bookingDate(Date.valueOf(recordToConvert.getBookingDate()))
                .fromAccountEntity(null)
                .toAccountEntity(null)
                .value(recordToConvert.getAmount().doubleValue())
                .build();
    }

    public static Account accountEntityToAccount(AccountEntity accountEntity) {
        // TODO: check for the narrowing conversion
        return new Account()
                .id(accountEntity.getId().intValue())
                .name(accountEntity.getName());
    }
}
