import React from "react";
import FormGroup from "react-bootstrap/lib/FormGroup";
import Col from "react-bootstrap/lib/Col";
import ControlLabel from "react-bootstrap/lib/ControlLabel";
import FormControl from "react-bootstrap/lib/FormControl";

class BaseAttrs extends React.Component {

    render() {
        let node = this.props.node;

        return (
            <div>
                <FormGroup controlId="formAttrsId">
                    <Col componentClass={ControlLabel} sm={2}>ID</Col>
                    <Col sm={10}>
                        <FormControl type="text"
                                     value={node.itemId}
                                     onChange={()=> {
                                     }}
                        />
                    </Col>
                </FormGroup>

                <FormGroup controlId="formAttrsName">
                    <Col componentClass={ControlLabel} sm={2}>Name</Col>
                    <Col sm={10}>
                        <FormControl type="text"
                                     value={node.displayName}
                                     onChange={()=> {
                                     }}
                        />
                    </Col>
                </FormGroup>

                <FormGroup controlId="formAttrsType">
                    <Col componentClass={ControlLabel} sm={2}>Item type</Col>
                    <Col sm={10}>
                        <FormControl type="text"
                                     value={node.itemType}
                                     onChange={()=> {
                                     }}
                        />
                    </Col>
                </FormGroup>

                <FormGroup controlId="formAttrsFiles">
                    <Col componentClass={ControlLabel} sm={2}>Files count</Col>
                    <Col sm={10}>
                        <FormControl type="text"
                                     value={node.filesCount}
                                     onChange={()=> {
                                     }}
                        />
                    </Col>
                </FormGroup>
            </div>
        );
    }
}

export default BaseAttrs;