import { CheckIcon, FunnelIcon } from '@heroicons/react/20/solid';
import React, { useEffect, useState } from 'react';
import MedicationLog from './MedicationLog';
import medsList from '../mockdata/medicationlist.json';

function MedicationList({ patientId }) {
	const [medicationList, setMedicationList] = useState([]);
	const [selectedMedicine, setSelectedMedicine] = useState({});
	const [visible, setVisible] = useState(false);

	const handleMedicationClick = (meds) => {
		//if user clicks on medication again, it will set visibility to false
		if (meds.id == selectedMedicine.id && visible) {
			setVisible(false);
		} else {
			setSelectedMedicine(meds);
			setVisible(true);
		}

		console.log(selectedMedicine);
		console.log(visible);
	};

	//GET call to retrieve medication list here using patientId
	useEffect(() => {
		setMedicationList(medsList);
	}, []);

	return (
		<>
			<h3 className='font-bold text-xl text-center mb-5'>Medication List</h3>
			<div className='max-h-96 overflow-y-auto px-8 w-6xl mx-auto overflow-x-auto mb-30 '>
				<table>
					<thead className='sticky top-0 z-10'>
						<tr>
							<th>Medication Name</th>
							<th>Dosage</th>
							<th>Frequency</th>
							<th>
								Status <FunnelIcon className='filter-btn' />
							</th>
							<th>
								Missed Dose <FunnelIcon className='filter-btn' />
							</th>
						</tr>
					</thead>
					<tbody>
						{medicationList.map((meds) => (
							<tr
								className='tr-list'
								onClick={() => handleMedicationClick(meds)}>
								<td>{meds.medicationName}</td>
								<td>{meds.dosage}</td>
								<td>{meds.frequency}</td>
								<td>{meds.isActive ? 'Active' : 'Inactive'}</td>
								<td>
									{meds.missedDose ? <CheckIcon className='check' /> : ''}
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			{visible && (
				<div className='mb-30'>
					<MedicationLog
						medication={selectedMedicine}
						patientId={patientId}
					/>
				</div>
			)}
		</>
	);
}

export default MedicationList;
