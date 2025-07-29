import { CheckIcon, FunnelIcon } from '@heroicons/react/20/solid';
import React, { useEffect, useRef, useState } from 'react';
import MedicationLog from './MedicationLog';
import FilterContainer from './FilterContainer';
import { getDynamicFilterOptions, applyFilter } from '../utils/filterUtil';
import medsList from '../mockdata/medicationlist.json';

function MedicationList({ patientId }) {
	const [medicationList, setMedicationList] = useState([]);
	const [displayedList, setDisplayedList] = useState([]);
	const [selectedMedicine, setSelectedMedicine] = useState({});
	const [visible, setVisible] = useState(false);
	const [filterKey, setFilterKey] = useState(null);
	const [selectedFilters, setSelectedFilters] = useState([]);
	const [uniqueOptions, setUniqueOptions] = useState([]);

	const filterRef = useRef();

	//defining fields that needs to be filtered first
	const filteredFields = ['isActive', 'missedDose'];
	const labelMap = {
		isActive: { true: 'Active', false: 'Inactive' },
		missedDose: { true: 'Missed', false: 'Compliant' },
	};

	//to create a more dynamic filter for easily scalable filtering later
	// desired format is [ { label: 'Active', field: 'isActive', value: true },]
	const dynamicFilterOptions = getDynamicFilterOptions(
		medicationList,
		filteredFields,
		labelMap
	);

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

	const handleFunnelClick = (col) => {
		let newKey;
		switch (col) {
			case 'Status':
				newKey = filterKey === 'isActive' ? null : 'isActive';
				break;
			case 'Missed Dose':
				newKey = filterKey === 'missedDose' ? null : 'missedDose';
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

	const handleFilterChange = (option) => {
		setSelectedFilters(
			(prevFilter) =>
				prevFilter.includes(option)
					? prevFilter.filter((o) => o !== option) //to remove from array if already exist
					: [...prevFilter, option] // to add if not in array
		);
	};

	//GET call to retrieve medication list here using patientId
	useEffect(() => {
		setMedicationList(medsList);
		setDisplayedList(medsList);
	}, []);

	useEffect(() => {
		const filtered = applyFilter(
			medicationList,
			dynamicFilterOptions,
			selectedFilters
		);
		setDisplayedList(filtered);
	}, [selectedFilters, medicationList]);

	useEffect(() => {
		const handleClickOutside = (event) => {
			//if filter-grid is open and whatever is being clicked is not the filter-grid, to close it
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
			<h3 className='font-bold text-xl text-center mb-3'>Medication List</h3>
			<div className='max-h-96 overflow-y-auto px-15 w-full mx-auto overflow-x-auto mb-20 '>
				<table>
					<thead className='sticky top-0 z-10'>
						<tr>
							<th>Medication Name</th>
							<th>Dosage</th>
							<th>Frequency</th>

							<th>
								<div className='relative inline-flex items-center gap-1'>
									Status
									<FunnelIcon
										onClick={() => handleFunnelClick('Status')}
										className='filter-btn'
									/>
								</div>
								{filterKey === 'isActive' ? (
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

							<th className='relative'>
								<div className='relative inline-flex items-center gap-1'>
									Missed Dose
									<FunnelIcon
										onClick={() => handleFunnelClick('Missed Dose')}
										className='filter-btn'
									/>
								</div>
								{filterKey === 'missedDose' ? (
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
						</tr>
					</thead>
					<tbody>
						{displayedList.map((meds) => (
							<tr
								className='tr-list'
								onClick={() => handleMedicationClick(meds)}>
								<td>{meds.medicationName}</td>
								<td>{meds.dosage}</td>
								<td>{meds.frequency}</td>
								<td>
									{meds.isActive ? (
										<div className='pos-status'>Active</div>
									) : (
										<div className='neg-status'>Inactive</div>
									)}
								</td>
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
