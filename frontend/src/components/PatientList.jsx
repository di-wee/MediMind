import React from 'react';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import { useState } from 'react';
import {
	PlusIcon,
	MagnifyingGlassIcon,
	UserPlusIcon,
} from '@heroicons/react/24/outline';
import allPatients from '../mockdata/patientlist.json';
import PatientGrid from '../components/PatientGrid';

function PatientList() {
	const [patients, setPatients] = useState(allPatients);
	const [searchTerm, setSearchTerm] = useState('');
	const [currentPage, setCurrentPage] = useState(1);
	const cardsPerPage = 9;

	// Filter patients based on search term - updated to search firstName, lastName, and NRIC
	const filteredPatients = patients.filter(
		(patient) =>
			patient.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
			patient.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
			patient.nric.toLowerCase().includes(searchTerm.toLowerCase()) ||
			patient.clinicName.toLowerCase().includes(searchTerm.toLowerCase())
	);

	const indexOfLastCard = currentPage * cardsPerPage;
	const indexOfFirstCard = indexOfLastCard - cardsPerPage;
	const currentPatients = filteredPatients.slice(
		indexOfFirstCard,
		indexOfLastCard
	);
	const totalPages = Math.ceil(filteredPatients.length / cardsPerPage);

	const handleRemove = (id) => {
		setPatients(patients.filter((patient) => patient.id !== id));
		if (currentPatients.length === 1 && currentPage > 1) {
			setCurrentPage(currentPage - 1);
		}
	};

	// Reset to page 1 when search changes
	const handleSearchChange = (e) => {
		setSearchTerm(e.target.value);
		setCurrentPage(1);
	};

	const handleAddPatient = () => {
		window.location.href = '/addpatient';
	};
	return (
		<>
			{/* Main Content */}
			<main className='ml-58 flex-1 p-8 bg-gray-50 min-h-screen'>
				{/* Page Header with Add Patients Button */}
				<div className='mb-8'>
					<div className='flex items-start justify-between'>
						<button
							onClick={() => (window.location.href = '/addpatient')}
							className='inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors duration-200'>
							<UserPlusIcon className='w-4 h-4 mr-2' />
							Add Patients
						</button>
					</div>
				</div>

				{/* Search Bar */}
				<div className='mb-6'>
					<div className='relative max-w-md'>
						<div className='absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none'>
							<MagnifyingGlassIcon className='h-5 w-5 text-gray-400' />
						</div>
						<input
							type='text'
							className='block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-2 focus:ring-gray-400 focus:border-transparent text-sm'
							placeholder='Search by name, NRIC, or clinic...'
							value={searchTerm}
							onChange={handleSearchChange}
						/>
					</div>
				</div>

				{/* Patient Grid */}
				<PatientGrid
					patients={currentPatients}
					onRemove={handleRemove}
					searchTerm={searchTerm}
					onAddPatient={handleAddPatient}
				/>

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
				{filteredPatients.length > 0 && (
					<div className='mt-6 bg-white rounded-lg border border-gray-200 p-4'>
						<p className='text-sm text-gray-600'>
							Showing {currentPatients.length} of {filteredPatients.length}{' '}
							patients
							{searchTerm && ` matching "${searchTerm}"`}
						</p>
					</div>
				)}

				{/* Floating Add Button */}
				<button
					onClick={() => (window.location.href = '/addpatient')}
					className='fixed bottom-8 right-8 w-16 h-16 bg-gray-800 hover:bg-gray-900 text-white rounded-full shadow-lg hover:shadow-xl focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-all duration-200 flex items-center justify-center group'>
					<PlusIcon className='w-6 h-6 group-hover:scale-110 transition-transform duration-200' />
				</button>
			</main>
		</>
	);
}

export default PatientList;
