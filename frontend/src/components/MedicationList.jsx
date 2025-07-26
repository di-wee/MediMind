import { CheckIcon, FunnelIcon } from '@heroicons/react/20/solid';
import React, { useState } from 'react';
import MedicationLog from './MedicationLog';

function MedicationList({ patientId }) {
	const [medication, setMedication] = useState({});
	const [visible, setVisible] = useState(false);

	const handleMedicationClick = (meds) => {
		//if user clicks on medication again, it will set visibility to false
		if (meds.id == medication.id && visible) {
			setVisible(false);
		} else {
			setMedication(meds);
			setVisible(true);
		}

		console.log(medication);
		console.log(visible);
	};

	//GET call to retrieve medication list here using patientId
	const medicationList = [
		{
			id: 'guid1',
			medicationName: 'Metformin',
			dosage: '500mg',
			frequency: '2x Daily',
			isActive: true,
			missedDose: true,
		},
		{
			id: 'guid2',
			medicationName: 'Lisinopril',
			dosage: '10mg',
			frequency: 'Once Daily',
			isActive: true,
			missedDose: false,
		},
		{
			id: 'guid3',
			medicationName: 'Atorvastatin',
			dosage: '20mg',
			frequency: 'Once Nightly',
			isActive: false,
			missedDose: true,
		},
	];
	return (
		<>
			<h3 className='font-bold text-xl text-center mb-5'>Medication List</h3>
			<div className='px-8 w-7xl mx-auto overflow-x-auto'>
				<table>
					<thead>
						<tr>
							<th>Medication Name</th>
							<th>Dosage</th>
							<th>Frequency</th>
							<th>
								Status <FunnelIcon className='inline-block size-2.5' />
							</th>
							<th>
								Missed Dose <FunnelIcon className='inline-block size-2.5' />
							</th>
						</tr>
					</thead>
					<tbody>
						{medicationList.map((medication) => (
							<tr
								className='tr-list'
								onClick={() => handleMedicationClick(medication)}>
								<td>{medication.medicationName}</td>
								<td>{medication.dosage}</td>
								<td>{medication.frequency}</td>
								<td>{medication.isActive ? 'Active' : 'Inactive'}</td>
								<td>
									{medication.missedDose ? <CheckIcon className='check' /> : ''}
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			{visible && (
				<div className='mt-30'>
					<MedicationLog
						medication={medication}
						patientId={patientId}
					/>
				</div>
			)}
		</>
	);
}

export default MedicationList;
