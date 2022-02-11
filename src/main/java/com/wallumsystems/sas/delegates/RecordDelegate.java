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

package com.wallumsystems.sas.delegates;

import com.wallumsystems.sas.entity.RecordEntity;
import com.wallumsystems.sas.entity.RevertingRecordEntity;
import com.wallumsystems.sas.entity.TaxRecordEntity;
import com.wallumsystems.sas.exception.AccountEntityNotFoundException;
import com.wallumsystems.sas.repository.RecordRepository;
import com.wallumsystems.sas.service.EntityToComponentConverter;
import com.wallumsystems.sas.service.RecordService;
import com.wallumsystems.sas.swagger.api.RecordsApiDelegate;
import com.wallumsystems.sas.swagger.model.NewRecord;
import com.wallumsystems.sas.swagger.model.Record;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordDelegate implements RecordsApiDelegate {

    private final RecordRepository recordRepository;

    private final RecordService recordService;

    private final EntityToComponentConverter entityToComponentConverter;

    public RecordDelegate(RecordRepository recordRepository, RecordService recordService, EntityToComponentConverter entityToComponentConverter) {
        this.recordRepository = recordRepository;
        this.recordService = recordService;
        this.entityToComponentConverter = entityToComponentConverter;
    }

    @Override
    public ResponseEntity<List<Record>> getRecord() {
        // TODO: should teh result be filtered for all the tax- and reverting records? And how are those handled?
        return new ResponseEntity<>(
                recordRepository.findAll().stream()
                        .map(entityToComponentConverter::recordEntityToRecord)
                        .toList()
                , HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Record> postRevertRecord(Integer id) {
        Optional<RecordEntity> recordEntityOptional = recordRepository.findById((long) id);
        if (recordEntityOptional.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        // reverting records and tax records can not be reverted
        if (recordEntityOptional.get() instanceof RevertingRecordEntity
                || recordEntityOptional.get() instanceof TaxRecordEntity)
            return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        RecordEntity recordEntity = recordEntityOptional.get();
        RevertingRecordEntity revertingRecord = recordService.getRevertingRecord(recordEntity);
        recordEntity.setRevertingRecord(revertingRecord);
        recordRepository.save(recordEntity);
        // TODO: should the reverting tax record be marked as tax record of the reverting record of the original record?
        if (recordEntity.getTaxRecord() != null) {
            TaxRecordEntity taxRecord = recordEntity.getTaxRecord();
            RevertingRecordEntity revertingTaxRecord = recordService.getRevertingRecord(taxRecord);
            taxRecord.setRevertingRecord(revertingTaxRecord);
            recordRepository.save(taxRecord);
        }
        return new ResponseEntity<>(
                entityToComponentConverter.recordEntityToRecord(recordEntityOptional.get()),
                HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<Record> postRecord(NewRecord newRecord) {
        try {
            RecordEntity recordEntity = entityToComponentConverter.newRecordToRecordEntity(newRecord);
            recordRepository.save(recordEntity);
            return new ResponseEntity<>(entityToComponentConverter.recordEntityToRecord(recordEntity), HttpStatus.CREATED);
        } catch (AccountEntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
