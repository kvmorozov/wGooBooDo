import React from "react";
import {List, Container, Label} from "semantic-ui-react";

class BaseAttrs extends React.Component {

    render() {
        let node = this.props.node;

        return (
            <Container>
                <List divided selection>
                    <List.Item>
                        <Label horizontal>ID</Label>
                        {node.itemId}
                    </List.Item>

                    <List.Item>
                        <Label horizontal>Name</Label>
                        {node.displayName}
                    </List.Item>

                    <List.Item>
                        <Label horizontal>Item type</Label>
                        {node.itemType}
                    </List.Item>

                    <List.Item>
                        <Label horizontal>Files count</Label>
                        {node.filesCount}
                    </List.Item>
                </List>
            </Container>);
    }
}

export default BaseAttrs;