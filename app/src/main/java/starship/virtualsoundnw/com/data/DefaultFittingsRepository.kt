/*
 * Copyright (C) 2025 David L. Dawes
 * Notice: As this license may require, be aware this file is new and had been added by David L. Dawes since cloning the original archive from github.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package starship.virtualsoundnw.com.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import starship.virtualsoundnw.com.data.local.database.Fitting
import starship.virtualsoundnw.com.data.local.database.FittingDao
import starship.virtualsoundnw.com.data.local.database.SensorType
import starship.virtualsoundnw.com.data.local.database.ComputerModel
import javax.inject.Inject

class DefaultFittingsRepository @Inject constructor(
    private val fittingDao: FittingDao
) : FittingsRepository {

    override fun getFittingForShip(shipId: Int): Flow<Fitting?> {
        return fittingDao.getFittingForShip(shipId)
    }

    override suspend fun updateFitting(fitting: Fitting) {
        // Delete existing fitting if any, then insert new one
        fittingDao.deleteFittingForShip(fitting.shipId)
        fittingDao.insertFitting(fitting)
    }

    override suspend fun updateSensorType(shipId: Int, sensorType: SensorType) {
        val existingFitting = fittingDao.getFittingForShip(shipId).first()
        val updatedFitting = existingFitting?.copy(sensorType = sensorType) 
            ?: Fitting(shipId = shipId, sensorType = sensorType)
        updateFitting(updatedFitting)
    }

    override suspend fun updateComputerModel(shipId: Int, computerModel: ComputerModel) {
        val existingFitting = fittingDao.getFittingForShip(shipId).first()
        val updatedFitting = existingFitting?.copy(computerModel = computerModel) 
            ?: Fitting(shipId = shipId, computerModel = computerModel)
        updateFitting(updatedFitting)
    }

    override suspend fun clearFittingForShip(shipId: Int) {
        fittingDao.deleteFittingForShip(shipId)
    }
}