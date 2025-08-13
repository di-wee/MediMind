import {
	ArrowsUpDownIcon,
	CheckIcon,
	FunnelIcon,
	XMarkIcon,
} from '@heroicons/react/20/solid';
import React, { useEffect, useRef, useState, useMemo } from 'react';
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
	// provides available filter options for the filter component
	const dynamicFilterOptions = useMemo(
		() => getDynamicFilterOptions(logList, filteredFields, labelMap),
		[logList]
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
						editedNote,
					}),
				}
			);

			if (response.ok) {
				setLogList((prev) =>
					prev.map((log) =>
						log.id === editingRowId ? { ...log, doctorNotes: editedNote } : log
					)
				);
				setDisplayList((prev) =>
					prev.map((log) =>
						log.id === editingRowId ? { ...log, doctorNotes: editedNote } : log
					)
				);
				//re-initialising state
				setEditingRowId(null);
				setEditedNote('');
			}
		} catch (err) {
			console.error('Error in saving doctor notes: ', err);
		}

		alert('Doctor note have been saved!');
	};

	//toggles the filter dropdown for a specific column
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

			//to show  available filter choices eg. 'Taken', 'Not Taken'
			setUniqueOptions(updatedOptions.map((op) => op.label));
		}
	};
	//to be passed down to filter component
	// manage which filter options are currently selected
	const handleFilterChange = (option) => {
		//if the option is already in the array, remove it, else add it
		// multiple filters can be active
		setSelectedFilters(
			(prevFilter) =>
				prevFilter.includes(option)
					? prevFilter.filter((o) => o !== option) //to remove from array if already exist
					: [...prevFilter, option] // to add if not in array
		);
	};

	useEffect(() => {
		let filtered = applyFilter(logList, dynamicFilterOptions, selectedFilters);

		filtered = [...filtered].sort((a, b) => {
			let aVal, bVal;
			if (sortConfig.column === 'date') {
				aVal = parseDateTime(a.loggedDate, a.scheduledTime);
				bVal = parseDateTime(b.loggedDate, b.scheduledTime);
			} else if (sortConfig.column === 'time') {
				const parseTime = (time) => {
					if (!time) return 0; // prevent undefined error
					const rawTime = time.replace(' HRs', ''); // 0800
					const hours = parseInt(rawTime.slice(0, 2), 10); //8
					const minutes = parseInt(rawTime.slice(2), 10); //0
					const totalMinutes = hours * 60 + minutes;
					return totalMinutes;
				};
				aVal = parseTime(a.scheduledTime);
				bVal = parseTime(b.scheduledTime);
			}
			return sortConfig.order === 'asc' ? aVal - bVal : bVal - aVal;
		});

		setDisplayList(filtered);
	}, [selectedFilters, logList, sortConfig, dynamicFilterOptions]);

	//handle column sorting
	//if the column is the same as the previous column, toggle the order
	//if the column is different, set the column to the new column and set the order to asc
	const handleSort = (column) => {
		setSortConfig((prev) => ({
			column: column,
			order: prev.column === column && prev.order === 'asc' ? 'desc' : 'asc',
		}));
	};

	//GET call here to extract medicationLog using medication.id and patientId, useState to store
	//medicationlog into logList

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
					//sorting the log by date
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

	// closing of filter container on clicking outside of the event.target
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
