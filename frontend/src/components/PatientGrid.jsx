import { PlusIcon, TrashIcon } from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';

export default function PatientGrid({
	patients,
	onRemove,
	searchTerm,
	onAddPatient,
}) {
	const navigate = useNavigate();
	const handleViewDetails = (patientId) => {
		navigate(`/patient/${patientId}`, { replace: true });
	};

	return (
		<div className='flex-1 flex flex-col w-full'>
			<div
				className="grid grid-cols-3 sm:grid-cols-2 xl:grid-cols-3 gap-6 mb-8 content-start w-full"
				style={{ minHeight: '600px' }}
				>
				{patients.map((patient) => (
					<div
						key={patient.id}
						className="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200 hover:border-gray-300 flex flex-col h-fit w-full min-w-[450px] max-w-[450px] mx-auto flex-shrink-0">
						<div className='p-5 flex flex-col h-full'>
							{/* Patient Header */}
							<div className='flex items-start justify-between mb-4'>
								<div className='flex-1 min-w-0'>
									<h3 className='text-lg font-bold text-gray-900 truncate mb-1'>
										{patient.firstName} {patient.lastName}
									</h3>
									<div className='flex items-baseline space-x-3'>
										<p className='text-sm text-gray-500 font-medium'>
											NRIC: ****{patient.nric.slice(-4)}
										</p>
										{/* Gender Badge */}
										<span
											className={`inline-flex items-center justify-center w-6 h-6 rounded-full text-xs font-bold text-white ${
												patient.gender === 'Male'
													? 'bg-blue-500'
													: 'bg-pink-500'
											}`}>
											{patient.gender === 'Male' ? 'M' : 'F'}
										</span>
									</div>
								</div>

								{/* Remove Button */}
								<button
									onClick={() => onRemove(patient.id)}
									className='p-2.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors duration-200'
									title='Remove patient'>
									<TrashIcon className='w-5 h-5' />
								</button>
							</div>

							{/* Patient Details */}
							<div className='mb-6 flex-grow'>
								{/* Divider Line */}
								<div className='border-t border-gray-200 mx-4 mb-6'></div>

								{/* Information Table */}
								<div className='space-y-4'>
									<div className='grid grid-cols-2 gap-6'>
										<div>
											<p className='text-xs font-medium text-gray-500 uppercase tracking-wider mb-1'>
												Date of Birth
											</p>
											<p className='text-sm font-semibold text-gray-900'>
												{patient.dob}
											</p>
										</div>
										<div>
											<p className='text-xs font-medium text-gray-500 uppercase tracking-wider mb-1'>
												Age
											</p>
											<p className='text-sm font-semibold text-gray-900'>
												{(() => {
													const today = new Date();
													const birthDate = new Date(patient.dob);
													let age =
														today.getFullYear() - birthDate.getFullYear();
													const monthDiff =
														today.getMonth() - birthDate.getMonth();
													if (
														monthDiff < 0 ||
														(monthDiff === 0 &&
															today.getDate() < birthDate.getDate())
													) {
														age--;
													}
													return age;
												})()}
											</p>
										</div>
									</div>

									<div className='grid grid-cols-2 gap-6'>
										<div>
											<p className='text-xs font-medium text-gray-500 uppercase tracking-wider mb-1'>
												Clinic
											</p>
											<p className='text-sm font-semibold text-gray-900'>
												{patient.clinic?.clinicName}
											</p>
										</div>
										<div>
											<p className='text-xs font-medium text-gray-500 uppercase tracking-wider mb-1'>
												Assigned Doctor
											</p>
											<p className='text-sm font-semibold text-gray-900'>
												Dr. {patient.doctor?.firstName} {patient.doctor?.lastName}
											</p>
										</div>
									</div>
								</div>
							</div>

							{/* View Details Button - Always at bottom */}
							<div className='mt-auto'>
								<button
									onClick={() => handleViewDetails(patient.id)}
									className='w-full inline-flex items-center justify-center px-4 py-2.5 border border-gray-200 text-sm font-medium rounded-lg text-gray-700 bg-gray-50 hover:bg-gray-100 hover:border-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-gray-400 transition-all duration-200'>
									View Details
								</button>
							</div>
						</div>
					</div>
				))}
			</div>

			{/* Empty State */}
			{patients.length === 0 && (
				<div className='flex-1 flex items-center justify-center'>
					<div className='text-center'>
						<div className='w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4'>
							<svg
								className='w-12 h-12 text-gray-400'
								fill='none'
								stroke='currentColor'
								viewBox='0 0 24 24'>
								<path
									strokeLinecap='round'
									strokeLinejoin='round'
									strokeWidth={1}
									d='M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z'
								/>
							</svg>
						</div>
						<h3 className='text-lg font-medium text-gray-900 mb-2'>
							No patients found
						</h3>
						<p className='text-gray-500 mb-4'>
							{searchTerm
								? 'Try adjusting your search terms.'
								: 'No assigned patients yet.'}
						</p>
						{!searchTerm && (
							<button
								onClick={(e) => onAddPatient(e)}
								className='inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors duration-200'>
								<PlusIcon className='w-4 h-4 mr-2' />
								Add Your First Patient
							</button>
						)}
					</div>
				</div>
			)}
		</div>
	);
}