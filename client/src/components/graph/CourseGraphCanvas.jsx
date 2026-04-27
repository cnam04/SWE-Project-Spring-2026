import { useMemo } from 'react'
import { Background, Controls, MiniMap, ReactFlow } from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import applyCourseGraphLayout from './layout/applyCourseGraphLayout'
import CourseNode from './nodes/CourseNode'
import OperatorNode from './nodes/OperatorNode'
import './graphStyles.css'

const NODE_TYPES = {
	courseNode: CourseNode,
	operatorNode: OperatorNode,
}

export default function CourseGraphCanvas({ graphData, isLoading, error }) {
	const { nodes, edges, hasNoPrerequisites } = useMemo(() => {
		const layoutedGraph = applyCourseGraphLayout(graphData)
		const nodeCount = layoutedGraph.nodes.length
		const edgeCount = layoutedGraph.edges.length

		return {
			nodes: layoutedGraph.nodes,
			edges: layoutedGraph.edges,
			hasNoPrerequisites: nodeCount > 0 && (edgeCount === 0 || nodeCount === 1),
		}
	}, [graphData])

	if (isLoading) {
		return (
			<div className="graph-canvas-placeholder prereq-graph-canvas">
				<p className="has-text-grey is-size-7 has-text-centered mb-0">Generating graph...</p>
			</div>
		)
	}

	if (error) {
		return (
			<div className="graph-canvas-placeholder prereq-graph-canvas">
				<p className="has-text-danger is-size-7 has-text-centered mb-0">{error}</p>
			</div>
		)
	}

	if (!graphData) {
		return (
			<div className="graph-canvas-placeholder prereq-graph-canvas">
				<p className="has-text-grey is-size-7 has-text-centered mb-0">
					Select a course and click Generate Graph to view prerequisites.
				</p>
			</div>
		)
	}

	if (!nodes.length) {
		return (
			<div className="graph-canvas-placeholder prereq-graph-canvas">
				<p className="has-text-danger is-size-7 has-text-centered mb-0">Graph response did not include nodes.</p>
			</div>
		)
	}

	return (
		<div className="course-graph-shell prereq-graph-canvas">
			{hasNoPrerequisites ? (
				<p className="course-graph-note is-size-7 has-text-grey mb-0">
					This course has no prerequisites.
				</p>
			) : null}
			<div className="course-graph-flow">
				<ReactFlow
					nodes={nodes}
					edges={edges}
					nodeTypes={NODE_TYPES}
					fitView
					minZoom={0.25}
					maxZoom={1.6}
					nodesDraggable={false}
					nodesConnectable={false}
					elementsSelectable={false}
					proOptions={{ hideAttribution: true }}
				>
					<MiniMap pannable zoomable />
					<Controls showInteractive={false} />
					<Background color="#d8e0ea" gap={18} size={1} />
				</ReactFlow>
			</div>
		</div>
	)
}
