import { ReactFlow, Background, Controls } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import '../styles/ReactFlowVisualization.css';

export default function ReactFlowVisualization() {
  return (
    <div className="react-flow-wrapper">
        <ReactFlow>
            <Background />
            <Controls />
        </ReactFlow>
    </div>

  )
}