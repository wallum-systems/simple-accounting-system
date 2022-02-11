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

package com.wallumsystems.sas.service;

import com.wallumsystems.sas.entity.AccountEntity;
import com.wallumsystems.sas.entity.RecordEntity;
import com.wallumsystems.sas.entity.RevertingRecordEntity;
import com.wallumsystems.sas.entity.TaxRecordEntity;
import com.wallumsystems.sas.exception.AccountEntityNotFoundException;
import com.wallumsystems.sas.repository.AccountRepository;
import com.wallumsystems.sas.swagger.model.Account;
import com.wallumsystems.sas.swagger.model.NewRecord;
import com.wallumsystems.sas.swagger.model.Record;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class EntityToComponentConverter {

    private final AccountRepository accountRepository;

    public EntityToComponentConverter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Record recordEntityToRecord(RecordEntity recordEntity) {
        // TODO: check for the narrowing conversion
        Record resultingRecord = new Record()
                .id(recordEntity.getId().intValue())
                .description(recordEntity.getDescription())
                .from(accountEntityToAccount(recordEntity.getFromAccountEntity()))
                .to(accountEntityToAccount(recordEntity.getToAccountEntity()))
                .amount(BigDecimal.valueOf(recordEntity.getValue()))
                .bookingDate(recordEntity.getBookingDate().toLocalDate())
                .creationTime(recordEntity.getCreationTime().toInstant().atOffset(ZoneOffset.UTC));
        if (recordEntity.getTaxRecord() != null)
            resultingRecord.taxRecord(recordEntity.getTaxRecord().getId().intValue());
        if (recordEntity.getRevertingRecord() != null)
            resultingRecord.revertingRecord(recordEntity.getRevertingRecord().getId().intValue());
        if (recordEntity instanceof TaxRecordEntity taxRecord)
            resultingRecord.taxedRecord(taxRecord.getRecordEntity().getId().intValue());
        if (recordEntity instanceof RevertingRecordEntity revertingRecord)
            resultingRecord.revertedRecord(revertingRecord.getRecordEntity().getId().intValue());
        return resultingRecord;
    }

    public RecordEntity newRecordToRecordEntity(NewRecord newRecord) throws AccountEntityNotFoundException {
        Optional<AccountEntity> optionalFromAccount = accountRepository.findById(Long.valueOf(newRecord.getFrom().getId()));
        Optional<AccountEntity> optionalToAccount = accountRepository.findById(Long.valueOf(newRecord.getTo().getId()));
        RecordEntity resultingRecord = RecordEntity.builder()
                .description(newRecord.getDescription())
                .fromAccountEntity(optionalFromAccount.orElseThrow(AccountEntityNotFoundException::new))
                .toAccountEntity(optionalToAccount.orElseThrow(AccountEntityNotFoundException::new))
                .value(newRecord.getAmount().doubleValue())
                .bookingDate(Date.valueOf(newRecord.getBookingDate()))
                .build();
        if (newRecord.getTaxRecord() != null)
            resultingRecord.setTaxRecord(recordToTaxRecordEntity(newRecord.getTaxRecord()));
        return resultingRecord;
    }

    public TaxRecordEntity recordToTaxRecordEntity(Record recordToConvert) throws AccountEntityNotFoundException {
        Optional<AccountEntity> optionalFromAccount = accountRepository.findById(Long.valueOf(recordToConvert.getFrom().getId()));
        Optional<AccountEntity> optionalToAccount = accountRepository.findById(Long.valueOf(recordToConvert.getTo().getId()));
        return TaxRecordEntity.builder()
                .description(recordToConvert.getDescription())
                .bookingDate(Date.valueOf(recordToConvert.getBookingDate()))
                .fromAccountEntity(optionalFromAccount.orElseThrow(AccountEntityNotFoundException::new))
                .toAccountEntity(optionalToAccount.orElseThrow(AccountEntityNotFoundException::new))
                .value(recordToConvert.getAmount().doubleValue())
                .build();
    }

    public Account accountEntityToAccount(AccountEntity accountEntity) {
        // TODO: check for the narrowing conversion
        return new Account()
                .id(accountEntity.getId().intValue())
                .name(accountEntity.getName());
    }
}
