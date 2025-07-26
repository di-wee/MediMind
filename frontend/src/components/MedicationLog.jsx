import { CheckIcon, FunnelIcon, XMarkIcon } from '@heroicons/react/20/solid';
import React from 'react';

function MedicationLog({ medication, patientId }) {
	//GET call here to extract medicationLog using medication.id and patientId
	const medicationLog = [
		{
			id: 'guid4',
			date: '20-07-2025',
			time: '0800 HRs',
			taken: true,
			notes: '',
		},
		{
			id: 'guid5',
			date: '20-07-2025',
			time: '0800 HRs',
			taken: false,
			notes: 'fell asleep',
		},
		{
			id: 'guid6',
			date: '21-07-2025',
			time: '0800 HRs',
			taken: true,
			notes: '',
		},
		{
			id: 'guid7',
			date: '21-07-2025',
			time: '2000 HRs',
			taken: true,
			notes: '',
		},
		{
			id: 'guid8',
			date: '22-07-2025',
			time: '0800 HRs',
			taken: true,
			notes: '',
		},
		{
			id: 'guid9',
			date: '22-07-2025',
			time: '2000 HRs',
			taken: false,
			notes:
				'forgot to bring medication out testing super long note here blablablbal',
		},
	];

	return (
		<>
			<h3 className='font-bold text-xl text-center mb-1'>
				Medication Intake Log
			</h3>
			<h6 className='text-center mb-5'>
				<i>
					[<b>{medication.medicationName}</b> - {medication.dosage},{' '}
					{medication.frequency}]
				</i>
			</h6>
			<div className='px-8 w-7xl mx-auto overflow-x-auto'>
				<table>
					<thead>
						<tr>
							<th>Date</th>
							<th>Time</th>
							<th>
								Taken <FunnelIcon className='inline-block size-2.5' />
							</th>
							<th>Notes</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						{medicationLog.map((log) => (
							<tr>
								<td>{log.date}</td>
								<td>{log.time}</td>
								<td>
									{log.taken ? (
										<CheckIcon className='log-check' />
									) : (
										<XMarkIcon className='log-x' />
									)}
								</td>
								<td className='max-w-50 break-words whitespace-normal overflow-x-auto'>
									{log.notes}
								</td>
								<td>
									<button className='btn-edit'>Edit</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
		</>
	);
}

export default MedicationLog;
