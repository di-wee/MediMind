import React from 'react';
import { useParams } from 'react-router';

function MedicationLog() {
	const { patientId, medicationId } = useParams();

	return <div></div>;
}

export default MedicationLog;
