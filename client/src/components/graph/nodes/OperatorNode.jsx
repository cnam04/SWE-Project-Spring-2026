import { Handle, Position } from '@xyflow/react'

export default function OperatorNode({ data, sourcePosition, targetPosition }) {
	const operatorText = String(data?.operator || data?.label || 'OR').toUpperCase()
	const operatorClass = operatorText === 'AND' ? 'is-and' : 'is-or'

	return (
		<div className={`operator-node ${operatorClass}`}>
			<Handle className="graph-node-handle" type="target" position={targetPosition || Position.Left} />
			<div className="operator-node-label">{operatorText}</div>
			<Handle className="graph-node-handle" type="source" position={sourcePosition || Position.Right} />
		</div>
	)
}
