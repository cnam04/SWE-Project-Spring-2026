import dagre from '@dagrejs/dagre'
import { MarkerType, Position } from '@xyflow/react'

const NODE_DIMENSIONS = {
	courseNode: { width: 250, height: 124 },
	operatorNode: { width: 120, height: 72 },
	default: { width: 220, height: 100 },
}

function normalizeDirection(direction) {
	const normalized = String(direction || '').toUpperCase()
	if (normalized === 'RL' || normalized === 'TB' || normalized === 'BT') {
		return normalized
	}

	return 'LR'
}

function resolveHandlePositions(direction) {
	if (direction === 'RL') {
		return { sourcePosition: Position.Left, targetPosition: Position.Right }
	}

	if (direction === 'TB') {
		return { sourcePosition: Position.Bottom, targetPosition: Position.Top }
	}

	if (direction === 'BT') {
		return { sourcePosition: Position.Top, targetPosition: Position.Bottom }
	}

	return { sourcePosition: Position.Right, targetPosition: Position.Left }
}

function resolveNodeSize(node) {
	const type = String(node?.type || '')
	return NODE_DIMENSIONS[type] || NODE_DIMENSIONS.default
}

function buildLayoutGraph(nodes, edges, direction) {
	const graph = new dagre.graphlib.Graph({ multigraph: true, compound: false })

	graph.setGraph({
		rankdir: direction,
		ranksep: 260,
		nodesep: 160,
		edgesep: 120,
		marginx: 72,
		marginy: 72,
		ranker: 'network-simplex',
	})
	graph.setDefaultEdgeLabel(() => ({}))

	nodes.forEach((node) => {
		const { width, height } = resolveNodeSize(node)
		graph.setNode(String(node.id), { width, height })
	})

	const nodeIds = new Set(nodes.map((node) => String(node.id)))
	edges.forEach((edge, index) => {
		const source = String(edge?.source || '')
		const target = String(edge?.target || '')
		if (!nodeIds.has(source) || !nodeIds.has(target)) {
			return
		}

		graph.setEdge(source, target, {}, String(edge?.id || `edge-${index}`))
	})

	return graph
}

function buildLayoutedNodes(nodes, graph, sourcePosition, targetPosition) {
	return nodes.map((node) => {
		const id = String(node.id)
		const { width, height } = resolveNodeSize(node)
		const dagreNode = graph.node(id)

		const x = dagreNode ? dagreNode.x - width / 2 : 0
		const y = dagreNode ? dagreNode.y - height / 2 : 0

		return {
			...node,
			position: { x, y },
			sourcePosition,
			targetPosition,
			data: node?.data && typeof node.data === 'object' ? node.data : {},
		}
	})
}

function buildLayoutedEdges(edges, nodes) {
	const nodeIdSet = new Set(nodes.map((node) => String(node.id)))

	return edges
		.filter((edge) => nodeIdSet.has(String(edge?.source || '')) && nodeIdSet.has(String(edge?.target || '')))
		.map((edge, index) => ({
			...edge,
			id: edge?.id || `edge-${index}`,
			type: edge?.type || 'smoothstep',
			pathOptions: edge?.pathOptions || { offset: 30, borderRadius: 18 },
			style: {
				stroke: '#5f6b7a',
				strokeWidth: 1.9,
				...(edge?.style || {}),
			},
			markerEnd: edge?.markerEnd || {
				type: MarkerType.ArrowClosed,
				color: '#5f6b7a',
			},
		}))
}

export default function applyCourseGraphLayout(graphData) {
	const nodes = Array.isArray(graphData?.nodes) ? graphData.nodes : []
	const edges = Array.isArray(graphData?.edges) ? graphData.edges : []

	if (!nodes.length) {
		return { nodes: [], edges: [] }
	}

	const direction = normalizeDirection(graphData?.layoutDirection)
	const graph = buildLayoutGraph(nodes, edges, direction)
	dagre.layout(graph)

	const { sourcePosition, targetPosition } = resolveHandlePositions(direction)
	const layoutedNodes = buildLayoutedNodes(nodes, graph, sourcePosition, targetPosition)
	const layoutedEdges = buildLayoutedEdges(edges, layoutedNodes)

	return {
		nodes: layoutedNodes,
		edges: layoutedEdges,
	}
}
