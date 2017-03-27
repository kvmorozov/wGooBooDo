import React from "react";
import Panel from "react-bootstrap/lib/Panel";

class Preview extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        let node = this.props.node;
        let text;

        if (node) {
            let o = {
                id: node.id,
                name: node.name,
                children: node.children ? node.children.length : 0,
                parent: node.parent ? node.parent.id : null,
                state: node.state
            };
            if (node.loadOnDemand !== undefined) {
                o.loadOnDemand = node.loadOnDemand;
            }
            text = JSON.stringify(o, null, 2);
        } else {
            text = '';
        }

        return (
            <Panel>
                {text}
            </Panel>
        );
    }
}

export default Preview;