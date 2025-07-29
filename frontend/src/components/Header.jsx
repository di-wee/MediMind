import {
	Square2StackIcon,
	Squares2X2Icon,
	UserCircleIcon,
} from '@heroicons/react/20/solid';
import { Link } from 'react-router-dom';

export default function PageHeader({ title, subtitle }) {
	return (
		<div className='mb-8 shadow-xl py-8 px-8 w-full'>
			<div className='flex items-center justify-start'>
				<div>
					<div className='inline-flex  items-center gap-2'>
						{title === 'Assigned Patients' && (
							<Squares2X2Icon className='size-7' />
						)}
						{title === 'Account' && <UserCircleIcon className='size-7' />}
						<h2 className='text-3xl font-bold text-gray-900 '>{title}</h2>
					</div>

					{subtitle && <p className='text-gray-600'>{subtitle}</p>}
				</div>
			</div>
		</div>
	);
}
