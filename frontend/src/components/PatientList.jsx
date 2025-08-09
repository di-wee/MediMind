import React from 'react';
import { useState, useMemo, useEffect, useContext } from 'react';
import {
	MagnifyingGlassIcon,
	UserPlusIcon,
	Squares2X2Icon,
	ListBulletIcon,
} from '@heroicons/react/24/outline';
import PatientGrid from '../components/PatientGrid';
import PatientListView from '../components/PatientListView';
import { useNavigate } from 'react-router-dom';
import MediMindContext from '../context/MediMindContext';
import { API_BASE_URL } from '../utils/config';

function PatientList() {
	const [patients, setPatients] = useState([]);
	const [loading, setLoading] = useState(true);
	const [searchTerm, setSearchTerm] = useState('');
	const [currentPage, setCurrentPage] = useState(1);
	const [viewMode, setViewMode] = useState('list'); // 'grid' or 'list'
	const [itemsPerPage, setItemsPerPage] = useState(5);
	const [sortBy, setSortBy] = useState(null); // 'clinic-asc', 'clinic-desc', etc.

	const navigate = useNavigate();
	const { doctorDetails } = useContext(MediMindContext);

	// Fetch patients from API when doctor details are available
	useEffect(() => {
		const fetchPatients = async () => {
			if (!doctorDetails.mcrNo) return;

			try {
				setLoading(true);
				const response = await fetch(
					`${API_BASE_URL}api/patients/by-doctor/${doctorDetails.mcrNo}`,
					{
						method: 'GET',
						credentials: 'include',
					}
				);

				if (response.ok) {
					const patientData = await response.json();
					setPatients(patientData);
				} else {
					console.error('Failed to fetch patients');
					setPatients([]);
				}
			} catch (error) {
				console.error('Error fetching patients:', error);
				setPatients([]);
			} finally {
				setLoading(false);
			}
		};

		fetchPatients();
	}, [doctorDetails.mcrNo]);

	// Filter patients based on search term
	const filteredPatients = patients.filter(
		(patient) =>
			patient.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
			patient.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
			patient.nric.toLowerCase().includes(searchTerm.toLowerCase())
	);

	// Sort patients if sortBy is set
	const sortedPatients = useMemo(() => {
		if (!sortBy) return filteredPatients;

		const [field, direction] = sortBy.split('-');

		return [...filteredPatients].sort((a, b) => {
			let aValue, bValue;

			if (field === 'clinic') {
				aValue = a.clinic?.clinicName || '';
				bValue = b.clinic?.clinicName || '';

				if (direction === 'asc') {
					return aValue.localeCompare(bValue);
				} else {
					return bValue.localeCompare(aValue);
				}
			} else if (field === 'age') {
				// Calculate age for both patients
				const today = new Date();

				const aDate = new Date(a.dob);
				let aAge = today.getFullYear() - aDate.getFullYear();
				const aMonthDiff = today.getMonth() - aDate.getMonth();
				if (
					aMonthDiff < 0 ||
					(aMonthDiff === 0 && today.getDate() < aDate.getDate())
				) {
					aAge--;
				}

				const bDate = new Date(b.dob);
				let bAge = today.getFullYear() - bDate.getFullYear();
				const bMonthDiff = today.getMonth() - bDate.getMonth();
				if (
					bMonthDiff < 0 ||
					(bMonthDiff === 0 && today.getDate() < bDate.getDate())
				) {
					bAge--;
				}

				aValue = aAge;
				bValue = bAge;

				if (direction === 'asc') {
					return aValue - bValue;
				} else {
					return bValue - aValue;
				}
			} else if (field === 'name') {
				// Sort by full name (firstName + lastName)
				aValue = `${a.firstName} ${a.lastName}`;
				bValue = `${b.firstName} ${b.lastName}`;

				if (direction === 'asc') {
					return aValue.localeCompare(bValue);
				} else {
					return bValue.localeCompare(aValue);
				}
			}
		});
	}, [filteredPatients, sortBy]);

	const indexOfLastCard = currentPage * itemsPerPage;
	const indexOfFirstCard = indexOfLastCard - itemsPerPage;
	const currentPatients = sortedPatients.slice(
		indexOfFirstCard,
		indexOfLastCard
	);
	const totalPages = Math.ceil(sortedPatients.length / itemsPerPage);

	const handleRemove = async (id) => {
		try {
			const response = await fetch(
				`${API_BASE_URL}api/patients/${id}/unassign-doctor`,
				{
					method: 'PUT',
					credentials: 'include',
				}
			);

			if (response.ok) {
				// Remove patient from the list after successful unassignment
				setPatients(patients.filter((patient) => patient.id !== id));
				if (currentPatients.length === 1 && currentPage > 1) {
					setCurrentPage(currentPage - 1);
				}
				console.log('Patient unassigned successfully');
			} else {
				console.error('Failed to unassign patient');
				alert('Failed to unassign patient. Please try again.');
			}
		} catch (error) {
			console.error('Error unassigning patient:', error);
			alert('Error occurred while unassigning patient. Please try again.');
		}
	};

	// Reset to page 1 when search changes
	const handleSearchChange = (e) => {
		setSearchTerm(e.target.value);
		setCurrentPage(1);
	};

	// Reset to page 1 when items per page changes
	const handleItemsPerPageChange = (e) => {
		setItemsPerPage(Number(e.target.value));
		setCurrentPage(1);
	};

	const handleAddPatient = (e) => {
		e.preventDefault();
		navigate('/addpatient', { replace: true });
	};

	if (loading) {
		return (
			<main className='flex-1 p-8 px-15 bg-gray-50 min-h-screen w-full pt-10 flex items-center justify-center'>
				<div className='text-center'>
					<div className='animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4'></div>
					<p className='text-gray-600'>Loading patients...</p>
				</div>
			</main>
		);
	}

	return (
		<>
			{/* Main Content */}
			<main className='flex-1 p-8 px-15 bg-gray-50 min-h-screen w-full pt-10'>
				{/* Page Header with Search, View Toggle, Per Page Selector, and Add Patients Button */}
				<div className='mb-6'>
					<div className='flex justify-between items-center'>
						{/* Left side - Search */}
						<div className='relative max-w-md'>
							<div className='absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none'>
								<MagnifyingGlassIcon className='h-5 w-5 text-gray-400' />
							</div>
							<input
								type='text'
								className='block w-full min-w-xl pl-10 pr-3 py-3 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-2 focus:ring-gray-400 focus:border-transparent text-sm'
								placeholder='Search by name or NRIC...'
								value={searchTerm}
								onChange={handleSearchChange}
							/>
						</div>

						{/* Right side - Per Page Selector, View Toggle, and Add Button */}
						<div className='flex items-center space-x-4'>
							{/* Items Per Page Selector */}
							<div className='flex items-center space-x-2'>
								<label
									htmlFor='itemsPerPage'
									className='text-sm text-gray-700 font-medium'>
									Show:
								</label>
								<select
									id='itemsPerPage'
									value={itemsPerPage}
									onChange={handleItemsPerPageChange}
									className='border border-gray-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent'>
									<option value={5}>5</option>
									<option value={10}>10</option>
									<option value={20}>20</option>
									<option value={30}>30</option>
									<option value={40}>40</option>
									<option value={50}>50</option>
								</select>
								<span className='text-sm text-gray-700'>per page</span>
							</div>

							{/* View Toggle */}
							<div className='flex items-center bg-white rounded-lg border border-gray-300 p-1'>
								<button
									onClick={() => setViewMode('grid')}
									className={`inline-flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors duration-200 ${
										viewMode === 'grid'
											? 'bg-gray-800 text-white shadow-sm'
											: 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
									}`}
									title='Grid View'>
									<Squares2X2Icon className='w-4 h-4' />
								</button>
								<button
									onClick={() => setViewMode('list')}
									className={`inline-flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors duration-200 ${
										viewMode === 'list'
											? 'bg-gray-800 text-white shadow-sm'
											: 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
									}`}
									title='List View'>
									<ListBulletIcon className='w-4 h-4' />
								</button>
							</div>

							{/* Add Patients Button */}
							<button
								onClick={(e) => handleAddPatient(e)}
								className='inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors duration-200'>
								<UserPlusIcon className='w-4 h-4 mr-2' />
								Add Patients
							</button>
						</div>
					</div>
				</div>

				{/* Patient Display Container */}
				<div>
					{viewMode === 'grid' ? (
						<PatientGrid
							patients={currentPatients}
							onRemove={handleRemove}
							searchTerm={searchTerm}
							onAddPatient={handleAddPatient}
						/>
					) : (
						<PatientListView
							patients={currentPatients}
							onRemove={handleRemove}
							searchTerm={searchTerm}
							onAddPatient={handleAddPatient}
							sortBy={sortBy}
							onSort={setSortBy}
						/>
					)}
				</div>

				{/* Pagination */}
				{totalPages > 1 && (
					<div className='flex justify-center items-center space-x-2 mt-8'>
						<button
							onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
							disabled={currentPage === 1}
							className='px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200'>
							Previous
						</button>

						{[...Array(totalPages)].map((_, i) => (
							<button
								key={i}
								onClick={() => setCurrentPage(i + 1)}
								className={`px-3 py-2 text-sm font-medium rounded-lg transition-colors duration-200 ${
									currentPage === i + 1
										? 'text-white bg-gray-800 border border-gray-800'
										: 'text-gray-700 bg-white border border-gray-300 hover:bg-gray-50'
								}`}>
								{i + 1}
							</button>
						))}

						<button
							onClick={() =>
								setCurrentPage(Math.min(totalPages, currentPage + 1))
							}
							disabled={currentPage === totalPages}
							className='px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200'>
							Next
						</button>
					</div>
				)}

				{/* Summary */}
				{sortedPatients.length > 0 && (
					<div className='mt-6 bg-white rounded-lg border border-gray-200 p-4'>
						<div className='flex justify-between items-center'>
							<p className='text-sm text-gray-600'>
								Showing {currentPatients.length} of {sortedPatients.length}{' '}
								patients
								{searchTerm && ` matching "${searchTerm}"`}
								{' in ' + (viewMode === 'grid' ? 'grid' : 'list') + ' view'}
							</p>
							<p className='text-sm text-gray-500'>
								{itemsPerPage} per page • Page {currentPage} of {totalPages}
							</p>
						</div>
					</div>
				)}
			</main>
		</>
	);
}

export default PatientList;
