import { MarkerType, Position } from '@xyflow/react'

const HORIZONTAL_GAP = 320
const VERTICAL_GAP = 170
const PADDING = 56

function normalizeDirection(direction) {
	const normalized = String(direction || '').toUpperCase()
	if (normalized === 'RL' || normalized === 'TB' || normalized === 'BT') {
		return normalized
	}

	return 'LR'
}

function buildAdjacencyMaps(nodes, edges) {
	const nodeIds = new Set(nodes.map((node) => String(node.id)))
	const outgoing = {}
	const incoming = {}

	nodes.forEach((node) => {
		const id = String(node.id)
		outgoing[id] = []
		incoming[id] = []
	})

	edges.forEach((edge) => {
		const source = String(edge?.source || '')
		const target = String(edge?.target || '')

		if (!nodeIds.has(source) || !nodeIds.has(target)) {
			return
		}

		outgoing[source].push(target)
		incoming[target].push(source)
	})

	return { outgoing, incoming }
}

function computeDepthByTopologicalOrder(nodes, outgoing, incoming) {
	const ids = nodes.map((node) => String(node.id))
	const indegree = {}
	const depth = {}

	ids.forEach((id) => {
		indegree[id] = incoming[id]?.length || 0
		depth[id] = 0
	})

	const queue = ids.filter((id) => indegree[id] === 0).sort((left, right) => left.localeCompare(right))
	let queueIndex = 0
	let visitedCount = 0

	while (queueIndex < queue.length) {
		const currentId = queue[queueIndex]
		queueIndex += 1
		visitedCount += 1

		const nextNodes = outgoing[currentId] || []
		nextNodes.forEach((nextId) => {
			depth[nextId] = Math.max(depth[nextId], depth[currentId] + 1)
			indegree[nextId] -= 1

			if (indegree[nextId] === 0) {
				queue.push(nextId)
			}
		})
	}

	if (visitedCount !== ids.length) {
		for (let i = 0; i < ids.length; i += 1) {
			let changed = false

			ids.forEach((id) => {
				const sources = incoming[id] || []
				sources.forEach((sourceId) => {
					const candidateDepth = depth[sourceId] + 1
					if (candidateDepth > depth[id]) {
						depth[id] = candidateDepth
						changed = true
					}
				})
			})

			if (!changed) {
				break
			}
		}
	}

	return depth
}

function buildLayerOrdering(nodes, depth, incoming) {
	const groupedByDepth = {}

	nodes.forEach((node) => {
		const id = String(node.id)
		const layer = depth[id] || 0

		if (!groupedByDepth[layer]) {
			groupedByDepth[layer] = []
		}

		groupedByDepth[layer].push(id)
	})

	const orderedLayers = Object.keys(groupedByDepth)
		.map((layer) => Number(layer))
		.sort((left, right) => left - right)

	const orderInLayer = {}

	orderedLayers.forEach((layer) => {
		const ids = groupedByDepth[layer]

		ids.sort((left, right) => {
			const leftSources = incoming[left] || []
			const rightSources = incoming[right] || []

			const leftAverage = leftSources.length
				? leftSources.reduce((sum, sourceId) => sum + (orderInLayer[sourceId] ?? 0), 0) / leftSources.length
				: Number.POSITIVE_INFINITY

			const rightAverage = rightSources.length
				? rightSources.reduce((sum, sourceId) => sum + (orderInLayer[sourceId] ?? 0), 0) / rightSources.length
				: Number.POSITIVE_INFINITY

			if (leftAverage !== rightAverage) {
				return leftAverage - rightAverage
			}

			return left.localeCompare(right)
		})

		ids.forEach((id, index) => {
			orderInLayer[id] = index
		})
	})

	return { groupedByDepth, orderedLayers }
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

function computeNodePosition(direction, layerIndex, orderIndex, layerSize) {
	const centeredOffset = (orderIndex - (layerSize - 1) / 2)

	if (direction === 'TB' || direction === 'BT') {
		return {
			x: centeredOffset * HORIZONTAL_GAP,
			y: layerIndex * VERTICAL_GAP,
		}
	}

	return {
		x: layerIndex * HORIZONTAL_GAP,
		y: centeredOffset * VERTICAL_GAP,
	}
}

function applyDirectionAndPadding(rawPositions, direction) {
	const mirroredPositions = rawPositions.map((position) => {
		if (direction === 'RL') {
			return { x: -position.x, y: position.y }
		}

		if (direction === 'BT') {
			return { x: position.x, y: -position.y }
		}

		return position
	})

	const minX = Math.min(...mirroredPositions.map((position) => position.x), 0)
	const minY = Math.min(...mirroredPositions.map((position) => position.y), 0)

	return mirroredPositions.map((position) => ({
		x: position.x - minX + PADDING,
		y: position.y - minY + PADDING,
	}))
}

export default function applyCourseGraphLayout(graphData) {
	const nodes = Array.isArray(graphData?.nodes) ? graphData.nodes : []
	const edges = Array.isArray(graphData?.edges) ? graphData.edges : []

	if (!nodes.length) {
		return { nodes: [], edges: [] }
	}

	const direction = normalizeDirection(graphData?.layoutDirection)
	const { outgoing, incoming } = buildAdjacencyMaps(nodes, edges)
	const depth = computeDepthByTopologicalOrder(nodes, outgoing, incoming)
	const { groupedByDepth, orderedLayers } = buildLayerOrdering(nodes, depth, incoming)
	const { sourcePosition, targetPosition } = resolveHandlePositions(direction)

	const idToRawPosition = {}
	orderedLayers.forEach((layer) => {
		const ids = groupedByDepth[layer]
		ids.forEach((id, index) => {
			idToRawPosition[id] = computeNodePosition(direction, layer, index, ids.length)
		})
	})

	const positionsWithPadding = applyDirectionAndPadding(
		nodes.map((node) => idToRawPosition[String(node.id)] || { x: 0, y: 0 }),
		direction,
	)

	const layoutedNodes = nodes.map((node, index) => ({
		...node,
		position: positionsWithPadding[index],
		sourcePosition,
		targetPosition,
		data: node?.data && typeof node.data === 'object' ? node.data : {},
	}))

	const nodeIdSet = new Set(layoutedNodes.map((node) => String(node.id)))
	const layoutedEdges = edges
		.filter((edge) => nodeIdSet.has(String(edge?.source || '')) && nodeIdSet.has(String(edge?.target || '')))
		.map((edge, index) => ({
			...edge,
			id: edge?.id || `edge-${index}`,
			type: edge?.type || 'smoothstep',
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

	return {
		nodes: layoutedNodes,
		edges: layoutedEdges,
	}
}
