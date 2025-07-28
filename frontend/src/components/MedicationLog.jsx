import { CheckIcon, FunnelIcon, XMarkIcon } from '@heroicons/react/20/solid';
import React, { useEffect, useRef, useState } from 'react';
import { getDynamicFilterOptions, applyFilter } from '../utils/filterUtil';
import medicationLog from '../mockdata/medicationlog.json';
import FilterContainer from './FilterContainer';

function MedicationLog({ medication, patientId }) {
	//state management
	const [editingRowId, setEditingRowId] = useState(null);
	const [editedNote, setEditedNote] = useState('');
	const [logList, setLogList] = useState([]);
	const [displayList, setDisplayList] = useState([]);
	const [filterKey, setFilterKey] = useState(null);
	const [uniqueOptions, setUniqueOptions] = useState([]);
	const [selectedFilters, setSelectedFilters] = useState([]);

	const filterRef = useRef();

	const filteredFields = ['taken'];
	const labelMap = {
		taken: { true: 'Taken', false: 'Not Taken' },
	};

	//to create a more dynamic filter for easily scalable filtering later
	// desired format is [ { label: 'Active', field: 'isActive', value: true },]
	const dynamicFilterOptions = getDynamicFilterOptions(
		logList,
		filteredFields,
		labelMap
	);

	const parseDateTime = (date, time) => {
		const [day, month, year] = date.split('-'); // day:21 month: 07, year: 2025

		const rawTime = time.replace(' HRs', '').trim();
		let formatTime = rawTime;
		if (rawTime.length === 4) {
			formatTime = `${rawTime.slice(0, 2)}:${rawTime.slice(2)}`; //8:00
		}

		const formatDateTime = `${year}-${month}-${day}T${formatTime}`;

		return new Date(formatDateTime);
	};

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

	const handleFunnelClick = (col) => {
		let newKey;
		//using switch here for scalability
		switch (col) {
			case 'Taken':
				newKey = filterKey === 'taken' ? null : 'taken';
				break;
		}

		setFilterKey(newKey);

		//setting the filter options to the appropriate column
		if (newKey) {
			const updatedOptions = dynamicFilterOptions.filter(
				(op) => op.field === newKey
			);
			//just want the label eg. 'Taken', 'Not Taken'
			setUniqueOptions(updatedOptions.map((op) => op.label));
		}
	};
	//to be passed down to filter component
	const handleFilterChange = (option) => {
		setSelectedFilters(
			(prevFilter) =>
				prevFilter.includes(option)
					? prevFilter.filter((o) => o !== option) //to remove from array if already exist
					: [...prevFilter, option] // to add if not in array
		);
	};

	//GET call here to extract medicationLog using medication.id and patientId, useState to store
	//medicationlog into logList

	useEffect(() => {
		const sortedLog = [...medicationLog].sort((a, b) => {
			//converting to proper date-time format YYYY-MM-DD HHMM to be compared
			const dateA = parseDateTime(a.date, a.time);

			const dateB = parseDateTime(b.date, b.time);

			return dateB - dateA;
		});

		// console.log('sorted', sortedLog);
		// console.log('non-sorted', medicationLog);

		setLogList(sortedLog);
		setDisplayList(sortedLog);
	}, []);

	useEffect(() => {
		const filtered = applyFilter(
			logList,
			dynamicFilterOptions,
			selectedFilters
		);
		setDisplayList(filtered);
	}, [selectedFilters, logList]);

	useEffect(() => {
		const handleClickOutside = (event) => {
			if (filterRef.current && !filterRef.current.contains(event.target)) {
				setFilterKey(null);
			}
		};
		document.addEventListener('mousedown', handleClickOutside);

		return () => {
			document.removeEventListener('mousedown', handleClickOutside);
		};
	}, [filterRef]);

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
			<div className='max-h-96 overflow-y-auto px-8 w-6xl mx-auto overflow-x-auto'>
				<table>
					<thead className='sticky top-0 z-10'>
						<tr>
							<th>Date</th>
							<th>Time</th>
							<th>
								<div className='relative inline-flex items-center gap-1'>
									Taken
									<FunnelIcon
										onClick={() => handleFunnelClick('Taken')}
										className='filter-btn'
									/>
								</div>
								{filterKey === 'taken' ? (
									<div ref={filterRef}>
										<FilterContainer
											filterOptions={uniqueOptions}
											selectedFilters={selectedFilters}
											handleFilterChange={handleFilterChange}
										/>
									</div>
								) : (
									''
								)}
							</th>
							<th>Notes</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						{displayList.map((log) => (
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
