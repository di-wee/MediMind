import { CheckIcon, FunnelIcon, XMarkIcon } from '@heroicons/react/20/solid';
import React, { useEffect, useState } from 'react';

function MedicationLog({ medication, patientId }) {
	//state management
	const [editingRowId, setEditingRowId] = useState(null);
	const [editedNote, setEditedNote] = useState('');
	const [logList, setLogList] = useState([]);

	//GET call here to extract medicationLog using medication.id and patientId, useState to store
	//medicationlog into logList

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

	useEffect(() => {
		setLogList(medicationLog);
	}, []);

	const handleEditClick = (log) => {
		//storing the edited row id and note content into state
		setEditingRowId(log.id);
		setEditedNote(log.notes);
	};

	const handleSaveClick = () => {
		//this is to keep the state most up to date when being edited
		//
		//logic: using the prev state of the medication log, and mapping (rebasing it into a new
		// array) if the log.id matches the id of the row being edited, we will replace
		// the note key value with editedNote, else we will just show the existing log
		setLogList((prev) =>
			prev.map((log) =>
				log.id === editingRowId ? { ...log, notes: editedNote } : log
			)
		);
		//re-initialising state
		setEditingRowId(null);
		setEditedNote('');
	};

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
			<div className='px-8 w-6xl mx-auto overflow-x-auto'>
				<table>
					<thead>
						<tr>
							<th>Date</th>
							<th>Time</th>
							<th>
								Taken <FunnelIcon className='filter-btn' />
							</th>
							<th>Notes</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						{logList.map((log) => (
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
									{editingRowId === log.id ? (
										<textarea
											value={editedNote}
											onChange={(e) => setEditedNote(e.target.value)}
											className='input w-full border-1 border-sky-500 p-1.5 rounded-xs'
										/>
									) : (
										log.notes
									)}
								</td>
								<td>
									{editingRowId === log.id ? (
										<button
											className='btn-save'
											onClick={() => handleSaveClick()}>
											Save
										</button>
									) : (
										<button
											className='btn-edit'
											onClick={() => handleEditClick(log)}>
											Edit
										</button>
									)}
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
