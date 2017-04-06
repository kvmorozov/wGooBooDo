import React from "react";
import {Button, Icon, Container} from "semantic-ui-react/";
import BaseAttrs from "./baseAttrs";
import client from "./../restClient";
import BooksList from "./booksList";

class Preview extends React.Component {

    constructor(props) {
        super(props);

        this.update = this.update.bind(this);
    }

    update(node) {
        this.setState({node: node});
        if (node._links != null) {
            client({
                method: 'GET', path: node._links['books'].href
            }).then(
                response => {
                    this.refs.booksList.update(response.entity);
                }
            );
            if (node.refreshStatus == 'dirty')
                client({
                    method: 'GET', path: node._links['refresh'].href
                }).then(
                    response => {
                        this.setState({node: response.entity});
                    }
                )
        }
    }

    render() {
        if (this.state == null || this.state.node == null) {
            return <Container/>
        }
        else {
            let node = this.state.node;
            let catButtons = node.categories.map(category => <Button key={category.name}>{category.name}</Button>);

            let refreshStatusIcon = node.refreshStatus == 'dirty' ? 'warning circle' : 'check circle';

            return (
                <Container>
                    <Container textAlign='right'>
                        <Icon name={refreshStatusIcon}/>
                    </Container>
                    <BaseAttrs node={node}/>
                    {catButtons}
                    <BooksList ref="booksList"/>
                </Container>
            );
        }
    }
}

export default Preview;