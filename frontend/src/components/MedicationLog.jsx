import {
	ArrowsUpDownIcon,
	CheckIcon,
	FunnelIcon,
	XMarkIcon,
} from '@heroicons/react/20/solid';
import React, { useEffect, useRef, useState } from 'react';
import { getDynamicFilterOptions, applyFilter } from '../utils/filterUtil';
import FilterContainer from './FilterContainer';
import { API_BASE_URL } from '../utils/config';

function MedicationLog({ medication }) {
	//state management
	const [editingRowId, setEditingRowId] = useState(null);
	const [editedNote, setEditedNote] = useState('');
	const [logList, setLogList] = useState([]);
	const [displayList, setDisplayList] = useState([]);
	//filter state
	const [filterKey, setFilterKey] = useState(null);
	const [uniqueOptions, setUniqueOptions] = useState([]);
	const [selectedFilters, setSelectedFilters] = useState([]);

	//sort logic
	const [sortConfig, setSortConfig] = useState({
		column: 'date',
		order: 'desc',
	});

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
		if (!date || !time) return new Date(0);

		//transforming it into 0800HRs eg
		const rawTime = time.replace(' HRs', '');
		const hours = rawTime.slice(0, 2);
		const minutes = rawTime.slice(2);

		return new Date(`${date}T${hours}:${minutes}`);
	};

	const handleEditClick = (log) => {
		//storing the edited row id and note content into state
		setEditingRowId(log.id);
		setEditedNote(log.doctorNotes);
	};

	const handleSaveClick = async () => {
		try {
			const response = await fetch(
				API_BASE_URL + `api/logs/save/doctor-notes`,
				{
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json',
					},
					body: JSON.stringify({
						intakeHistoryId: editingRowId,
						doctorNotes: editedNote,
					}),
				}
			);

			if (response.ok) {
				// Update the log list with the new note
				setLogList((prevLogs) =>
					prevLogs.map((log) =>
						log.id === editingRowId ? { ...log, doctorNotes: editedNote } : log
					)
				);
				setDisplayList((prevLogs) =>
					prevLogs.map((log) =>
						log.id === editingRowId ? { ...log, doctorNotes: editedNote } : log
					)
				);
				setEditingRowId(null);
				setEditedNote('');
			}
		} catch (err) {
			console.error('Error saving doctor notes: ', err);
		}
	};

	const handleSort = (column) => {
		setSortConfig((prevConfig) => ({
			column,
			order:
				prevConfig.column === column && prevConfig.order === 'asc'
					? 'desc'
					: 'asc',
		}));
	};

	const handleFunnelClick = (col) => {
		let newKey;
		switch (col) {
			case 'Taken':
				newKey = filterKey === 'taken' ? null : 'taken';
				break;
		}

		setFilterKey(newKey);
		if (newKey) {
			const updatedOptions = dynamicFilterOptions.filter(
				(op) => op.field === newKey
			);
			setUniqueOptions(updatedOptions.map((op) => op.label));
		}
	};

	const handleFilterChange = (selectedValues) => {
		setSelectedFilters(selectedValues);
		const filteredData = applyFilter(logList, filterKey, selectedValues);
		setDisplayList(filteredData);
	};

	useEffect(() => {
		const fetchMedicationLog = async () => {
			try {
				const response = await fetch(
					API_BASE_URL + `api/medication/${medication.id}/logs`,
					{
						method: 'GET',
						headers: {
							'Content-Type': 'application/json',
						},
					}
				);

				if (response.ok) {
					const medicationLog = await response.json();

					const transformedLog = medicationLog.map((log) => {
						//"scheduledTime": "20:00:00"
						const formattedTime =
							log.scheduledTime.slice(0, 2) +
							log.scheduledTime.slice(3, 5) +
							' HRs';
						return {
							id: log.intakeHistoryId,
							loggedDate: log.loggedDate,
							scheduledTime: formattedTime,
							doctorNotes: log.doctorNotes || '',
							taken: log.taken,
							scheduleId: log.scheduleId,
						};
					});

					const sortedLog = [...transformedLog].sort(
						(a, b) =>
							parseDateTime(b.loggedDate, b.scheduledTime) -
							parseDateTime(a.loggedDate, a.scheduledTime)
					);

					setLogList(sortedLog);
					setDisplayList(sortedLog);
				}
			} catch (err) {
				console.error('Error in fetching medication log: ', err);
			}
		};
		fetchMedicationLog();
	}, [medication]);

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
			<h3 className='font-bold text-lg px-15 '>Medication Intake Log</h3>
			<h6 className='text-sm px-15 mb-5'>
				[<b>{medication.medicationName}</b> - {medication.intakeQuantity},{' '}
				{medication.frequency} times a day]
			</h6>
			<div className='max-h-96 w-full overflow-y-auto px-15 mx-auto overflow-x-auto mb-10'>
				<table>
					<thead className='sticky top-0 z-10'>
						<tr>
							<th>
								<div className='relative inline-flex items-center gap-1'>
									Date{' '}
									<ArrowsUpDownIcon
										onClick={() => handleSort('date')}
										className='inline-block size-4 cursor-pointer'
									/>
								</div>
							</th>
							<th>
								<div className='relative inline-flex items-center gap-1'>
									Time{' '}
									<ArrowsUpDownIcon
										onClick={() => handleSort('time')}
										className='inline-block size-4 cursor-pointer'
									/>
								</div>
							</th>
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
								<td>{log.loggedDate}</td>
								<td>{log.scheduledTime}</td>
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
										log.doctorNotes
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
