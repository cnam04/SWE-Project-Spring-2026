import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
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
	const graphShellRef = useRef(null)
	const [isFullscreen, setIsFullscreen] = useState(false)
	const [reactFlowInstance, setReactFlowInstance] = useState(null)

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

	const fitGraphInView = useCallback(() => {
		if (!reactFlowInstance || !nodes.length) {
			return
		}

		reactFlowInstance.fitView({ padding: 0.28, minZoom: 0.25, maxZoom: 1.1 })
	}, [nodes.length, reactFlowInstance])

	useEffect(() => {
		const timeoutId = setTimeout(() => {
			fitGraphInView()
		}, 120)

		return () => {
			clearTimeout(timeoutId)
		}
	}, [fitGraphInView, isFullscreen])

	useEffect(() => {
		const updateFullscreenState = () => {
			const fullscreenElement = document.fullscreenElement || document.webkitFullscreenElement
			setIsFullscreen(fullscreenElement === graphShellRef.current)
		}

		document.addEventListener('fullscreenchange', updateFullscreenState)
		document.addEventListener('webkitfullscreenchange', updateFullscreenState)

		return () => {
			document.removeEventListener('fullscreenchange', updateFullscreenState)
			document.removeEventListener('webkitfullscreenchange', updateFullscreenState)
		}
	}, [])

	const handleToggleFullscreen = useCallback(async () => {
		const shellElement = graphShellRef.current
		if (!shellElement) {
			return
		}

		const fullscreenElement = document.fullscreenElement || document.webkitFullscreenElement

		if (fullscreenElement === shellElement) {
			if (document.exitFullscreen) {
				await document.exitFullscreen()
			} else if (document.webkitExitFullscreen) {
				document.webkitExitFullscreen()
			}
			return
		}

		if (document.fullscreenElement && document.exitFullscreen) {
			await document.exitFullscreen()
		}

		if (shellElement.requestFullscreen) {
			await shellElement.requestFullscreen()
		} else if (shellElement.webkitRequestFullscreen) {
			shellElement.webkitRequestFullscreen()
		}
	}, [])

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
		<div
			ref={graphShellRef}
			className={`course-graph-shell prereq-graph-canvas${isFullscreen ? ' is-fullscreen' : ''}`}
		>
			<div className="course-graph-toolbar">
				{hasNoPrerequisites ? (
					<p className="course-graph-note is-size-7 has-text-grey mb-0">This course has no prerequisites.</p>
				) : (
					<span className="course-graph-note-placeholder" aria-hidden="true" />
				)}
				<button
					type="button"
					className="button is-small is-light course-graph-fullscreen-button"
					onClick={handleToggleFullscreen}
				>
					{isFullscreen ? 'Exit Fullscreen' : 'Fullscreen'}
				</button>
			</div>
			<div className="course-graph-flow">
				<ReactFlow
					nodes={nodes}
					edges={edges}
					nodeTypes={NODE_TYPES}
					fitView
					fitViewOptions={{ padding: 0.28, minZoom: 0.25, maxZoom: 1.1 }}
					onInit={setReactFlowInstance}
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
