import { Handle, Position } from '@xyflow/react'

const STATUS_LABELS = {
	completed: 'Completed',
	in_progress: 'In Progress',
	planned: 'Planned',
	not_taken: 'Not Taken',
}

function normalizeStatus(value) {
	const normalized = String(value || '').toLowerCase()
	if (normalized === 'completed' || normalized === 'in_progress' || normalized === 'planned' || normalized === 'not_taken') {
		return normalized
	}

	return 'none'
}

export default function CourseNode({ data, sourcePosition, targetPosition }) {
	const status = normalizeStatus(data?.status)
	const statusLabel = STATUS_LABELS[status] || ''
	const isTargetCourse = Boolean(data?.isTargetCourse)

	const nodeClassName = [
		'course-node',
		isTargetCourse ? 'is-target-course' : '',
		`status-${status}`,
	]
		.filter(Boolean)
		.join(' ')

	return (
		<div className={nodeClassName}>
			<Handle className="graph-node-handle" type="target" position={targetPosition || Position.Left} />
			<div className="course-node-code">{data?.courseCode || 'UNKNOWN'}</div>
			<div className="course-node-title">{data?.title || 'Untitled Course'}</div>
			<div className="course-node-meta-row">
				<span>{data?.credits !== null && data?.credits !== undefined ? `${data.credits} credits` : 'Credits N/A'}</span>
				<span>CRN {data?.crn || 'N/A'}</span>
			</div>
			{status !== 'none' ? (
				<div className={`course-node-status status-${status}`}>{statusLabel}</div>
			) : null}
			<Handle className="graph-node-handle" type="source" position={sourcePosition || Position.Right} />
		</div>
	)
}
