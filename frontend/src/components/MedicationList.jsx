import { CheckIcon, FunnelIcon } from '@heroicons/react/20/solid';
import React from 'react';

function MedicationList() {
	const medicationList = [
		{
			medicationName: 'Metformin',
			dosage: '500mg',
			frequency: '2x Daily',
			isActive: true,
			missedDose: true,
		},
		{
			medicationName: 'Lisinopril',
			dosage: '10mg',
			frequency: 'Once Daily',
			isActive: true,
			missedDose: false,
		},
		{
			medicationName: 'Atorvastatin',
			dosage: '20mg',
			frequency: 'Once Nightly',
			isActive: false,
			missedDose: true,
		},
	];
	return (
		<>
			<h3 className='font-bold text-center mb-5'>Medication List</h3>
			<div className='px-8 w-4xl mx-auto overflow-x-auto'>
				<table>
					<thead>
						<th>Medication Name</th>
						<th>Dosage</th>
						<th>Frequency</th>
						<th>
							Status <FunnelIcon className='inline-block size-2.5' />
						</th>
						<th>
							Missed Dose <FunnelIcon className='inline-block size-2.5' />
						</th>
					</thead>
					<tbody>
						{medicationList.map((medication) => (
							<tr>
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
		</>
	);
}

export default MedicationList;
